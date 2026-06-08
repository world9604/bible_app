#!/usr/bin/env python3
"""Build a TrueType font from extracted handwriting glyph bitmaps.

- Traces each glyph bitmap into rectilinear vector contours (no potrace needed).
- Normalizes size/baseline per source line.
- Assembles a .ttf with fontTools, mapping each Korean syllable to its glyph.
"""
import numpy as np
from PIL import Image
import os, json, sys

ROOT = os.path.dirname(os.path.abspath(__file__))
GLY = os.path.join(ROOT, "glyphs")
OUT = os.path.join(ROOT, "out"); os.makedirs(OUT, exist_ok=True)
sys.path.insert(0, os.path.join(ROOT, "pylibs"))

from fontTools.fontBuilder import FontBuilder
from fontTools.pens.ttGlyphPen import TTGlyphPen
from fontTools.misc.transform import Identity

UPEM = 1000
TARGET_ASC = 720.0   # font units from baseline to top of a full-height glyph
SB = 42              # side bearing (units)
SPACE_ADV = 300

def load_glyph_img(key):
    im = Image.open(os.path.join(GLY, key+".png")).convert("L")
    a = np.asarray(im)
    return a < 128   # True = ink

def smooth_ink(ink, up=4):
    """Upsample + antialias + threshold to round the pixel staircase."""
    from PIL import ImageFilter
    im = Image.fromarray((ink*255).astype(np.uint8))
    im = im.resize((max(1,im.width*up), max(1,im.height*up)), Image.LANCZOS)
    im = im.filter(ImageFilter.GaussianBlur(up*0.5))
    a = np.asarray(im) > 115
    return a, up

def trace_contours(ink):
    """Return list of contours; each is list of (x,y) grid points (image coords, y-down).
    Interior is on the right of each directed edge (clockwise in image space)."""
    H, W = ink.shape
    def isink(r,c):
        return 0<=r<H and 0<=c<W and ink[r,c]
    edges = {}  # (x,y) -> list of (x2,y2)
    def add(a,b):
        edges.setdefault(a,[]).append(b)
    for r in range(H):
        for c in range(W):
            if not ink[r,c]: continue
            TL=(c,r); TR=(c+1,r); BR=(c+1,r+1); BL=(c,r+1)
            if not isink(r-1,c): add(TL,TR)   # top
            if not isink(r,c+1): add(TR,BR)   # right
            if not isink(r+1,c): add(BR,BL)   # bottom
            if not isink(r,c-1): add(BL,TL)   # left
    contours=[]
    while edges:
        # start from any remaining edge
        start = next(iter(edges))
        loop=[start]; cur=start
        while True:
            nxts = edges.get(cur)
            if not nxts:
                break
            nxt = nxts.pop(0)
            if not edges[cur]: del edges[cur]
            cur = nxt
            if cur==start:
                break
            loop.append(cur)
        if len(loop)>=4:
            contours.append(simplify(loop))
    return contours

def dp(points, eps):
    """Douglas-Peucker on a closed polygon (list of (x,y))."""
    if len(points)<5: return points
    def rdp(pts):
        if len(pts)<3: return pts
        a=pts[0]; b=pts[-1]
        dx=b[0]-a[0]; dy=b[1]-a[1]; den=(dx*dx+dy*dy)**0.5 or 1e-9
        dmax=0; idx=0
        for i in range(1,len(pts)-1):
            d=abs(dx*(a[1]-pts[i][1])-dy*(a[0]-pts[i][0]))/den
            if d>dmax: dmax=d; idx=i
        if dmax>eps:
            l=rdp(pts[:idx+1]); r=rdp(pts[idx:])
            return l[:-1]+r
        return [a,b]
    # split closed loop at two far points to apply rdp on two arcs
    n=len(points); half=n//2
    arc1=rdp(points[:half+1]); arc2=rdp(points[half:]+[points[0]])
    out=arc1[:-1]+arc2[:-1]
    return out if len(out)>=3 else points

def simplify(loop):
    """Remove collinear points from a rectilinear loop."""
    pts=loop[:]
    out=[]
    n=len(pts)
    for i in range(n):
        a=pts[i-1]; b=pts[i]; c=pts[(i+1)%n]
        # keep b only if direction changes
        d1=(b[0]-a[0],b[1]-a[1]); d2=(c[0]-b[0],c[1]-b[1])
        if (d1[0]*d2[1]-d1[1]*d2[0])!=0 or (d1[0]*d2[0]+d1[1]*d2[1])<0:
            out.append(b)
    return out if len(out)>=3 else loop

def build():
    meta = json.load(open(os.path.join(ROOT,"glyphs.json")))
    meta = [g for g in meta if 'w' in g]   # skip empties
    # density cache
    def density(g):
        ink=load_glyph_img(g['key']);
        return ink.mean(), int(ink.sum())
    # choose best instance per char by a penalty that rejects merges/blobs/slivers
    DROP=1.3   # if best instance still scores worse than this, omit the char
    cand={}
    for g in meta: cand.setdefault(g['ch'],[]).append(g)
    best={}; dropped=[]
    for ch,gs in cand.items():
        scored=[]
        for g in gs:
            cap=max(8,g['base']-g['topcap'])
            w,h=g['w'],g['h']; ar=w/max(1,h)
            dens,ink=density(g)
            pen=0.0
            # excess size => merged neighbours (penalize hard). Do NOT penalize small
            # (받침 없는 글자 are legitimately short/narrow).
            pen += 2.5*max(0,(w-1.05*cap)/cap)
            pen += 2.5*max(0,(h-1.05*cap)/cap)
            # density: sparse => fragmented/merged spread; dense => blob
            if dens<0.18: pen += 12*(0.18-dens)
            if dens>0.55: pen += 8*(dens-0.55)
            if ar>1.9: pen += 1.5*(ar-1.9)
            if ar<0.32: pen += 1.5*(0.32-ar)
            if ink<35: pen += 2.0
            pen -= 0.0008*ink   # tie-break toward more complete glyphs
            if ch=='.':
                pen = abs(w-h) + abs(w-5)*0.5 + (3 if (w*h>120) else 0)
            scored.append((pen,g))
        scored.sort(key=lambda t:t[0])
        if scored[0][0] > DROP and ch!='.':
            dropped.append(ch); continue
        best[ch]=scored[0]
    chars=sorted(best.keys())
    print("unique chars:",len(chars),"| dropped(no clean instance):",
          "".join(dropped) if dropped else "none")

    fb = FontBuilder(UPEM, isTTF=True)
    glyph_order=[".notdef","space"]
    cmap={0x20:"space"}
    advances={".notdef":SPACE_ADV,"space":SPACE_ADV}
    pens={}

    # .notdef empty
    p=TTGlyphPen(None); pens[".notdef"]=p.glyph()
    p=TTGlyphPen(None); pens["space"]=p.glyph()

    for ch in chars:
        g=best[ch][1]
        ink=load_glyph_img(g['key'])
        # per-line scale keeps cross-line sizes consistent
        line_h = g['base']-g['topcap']
        scale = TARGET_ASC/max(8,line_h)
        sink, up = smooth_ink(ink)        # smoothed, upsampled by `up`
        eff = scale/up
        gh = sink.shape[0]
        contours=trace_contours(sink)
        if not contours:
            continue
        pen=TTGlyphPen(None)
        for cont in contours:
            cont = dp(cont, up*0.8)         # smooth the staircase
            if len(cont)<3: continue
            poly=[]
            for (x,y) in cont:
                Xf=SB + x*eff
                Yf=(gh-1-y)*eff            # bottom-align glyph to the baseline
                poly.append((round(Xf),round(Yf)))
            pen.moveTo(poly[0])
            for pt in poly[1:]:
                pen.lineTo(pt)
            pen.closePath()
        gname=f"uni{ord(ch):04X}"
        pens[gname]=pen.glyph()
        glyph_order.append(gname)
        cmap[ord(ch)]=gname
        adv = round(g['w']*scale + 2*SB)
        advances[gname]=adv

    fb.setupGlyphOrder(glyph_order)
    fb.setupCharacterMap(cmap)
    fb.setupGlyf(pens)
    metrics={gn:(advances.get(gn,SPACE_ADV),0) for gn in glyph_order}
    # set proper lsb from glyph xMin
    glyf=fb.font["glyf"]
    hmtx={}
    for gn in glyph_order:
        adv=advances.get(gn,SPACE_ADV)
        gl=glyf[gn]
        try: xmin=gl.xMin if hasattr(gl,'xMin') and gl.numberOfContours>0 else 0
        except Exception: xmin=0
        hmtx[gn]=(adv,xmin)
    fb.setupHorizontalMetrics(hmtx)
    fb.setupHorizontalHeader(ascent=800, descent=-200)
    fb.setupNameTable(dict(familyName="Sonsulssi Handwriting",
                           styleName="Regular",
                           psName="SonsulssiHandwriting-Regular"))
    fb.setupOS2(sTypoAscender=800, sTypoDescender=-200, usWinAscent=850, usWinDescent=220)
    fb.setupPost()
    out=os.path.join(OUT,"SonsulssiHandwriting-Regular.ttf")
    fb.save(out)
    print("saved",out,"glyphs",len(glyph_order))
    return out

if __name__=="__main__":
    build()
