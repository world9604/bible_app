#!/usr/bin/env python3
"""Segmentation v4: anchor on real word-gaps, then split each word into its
known syllable count (regular spacing within a word). Periods handled specially."""
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from scipy import ndimage
import os, json

ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, "..", "..", "references", "성경 어플 ", "1.jpg")
DBG = os.path.join(ROOT, "debug"); os.makedirs(DBG, exist_ok=True)
GLY = os.path.join(ROOT, "glyphs"); os.makedirs(GLY, exist_ok=True)

SLINES = [
    "나답게 산다. 나를 조용히 지킨다. 나를 숨기지",
    "않는다. 나에 대해 허세를 부리지 않는다.",
    "나를 함부로 내세워 자랑하지도 않는다.",
    "동시에 나만의 피해자인 양 자기연민을",
    "갖거나 자학하지도 않는다. 나만 중요하다고",
    "여기지 않는 버릇을 들인다. 남과 나를 비교",
    "하지 않는다. 이것들은 모두 정신적으로 좋은",
    "자세를 가진 사람의 특징이다.",
]

def load_ink():
    im = Image.open(SRC).convert("RGB")
    a = np.asarray(im).astype(int)
    lum = (0.299*a[...,0] + 0.587*a[...,1] + 0.114*a[...,2])
    x0,x1,y0,y1 = 74,656,168,567
    ink = (lum[y0:y1, x0:x1] < 95)
    ink = ndimage.binary_opening(ink, structure=np.ones((2,2)))
    return im, ink

def kmeans1d(vals,k,iters=80,weights=None):
    vals=np.asarray(vals,float); w=np.ones_like(vals) if weights is None else np.asarray(weights,float)
    centers=np.linspace(vals.min(),vals.max(),k); asg=np.zeros(len(vals),int)
    for _ in range(iters):
        asg=np.abs(vals[:,None]-centers[None,:]).argmin(1); new=centers.copy()
        for j in range(k):
            m=asg==j
            if m.any(): new[j]=np.average(vals[m],weights=w[m])
        if np.allclose(new,centers):break
        centers=new
    return asg,centers

def zero_runs(profile, thresh=0):
    runs=[]; s=None
    for i,v in enumerate(profile):
        if v<=thresh and s is None: s=i
        if v>thresh and s is not None: runs.append((s,i)); s=None
    if s is not None: runs.append((s,len(profile)))
    return runs

def main():
    im, ink = load_ink()
    lab,n=ndimage.label(ink); objs=ndimage.find_objects(lab)
    comps=[]
    for i,sl in enumerate(objs):
        if sl is None: continue
        ys,xs=sl; area=int((lab[sl]==(i+1)).sum())
        if area<10: continue
        comps.append(dict(lbl=i+1,x0=xs.start,x1=xs.stop,y0=ys.start,y1=ys.stop,
                          area=area,cx=(xs.start+xs.stop)/2,cy=(ys.start+ys.stop)/2,
                          h=ys.stop-ys.start))
    asg,cent=kmeans1d([c['cy'] for c in comps],8,weights=[c['area'] for c in comps])
    order=np.argsort(cent); remap={o:i for i,o in enumerate(order)}
    for c,a in zip(comps,asg): c['line']=remap[a]

    glyphs=[]
    Wpix=ink.shape[1]
    for li in range(8):
        cs=sorted([c for c in comps if c['line']==li],key=lambda c:c['cx'])
        words=SLINES[li].split(' ')
        nwords=len(words)
        ly0=min(c['y0'] for c in cs); ly1=max(c['y1'] for c in cs)
        # column profile for this line
        prof=np.zeros(Wpix)
        mask=np.zeros_like(ink)
        for c in cs: mask|=(lab==c['lbl'])
        prof=mask[ly0:ly1,:].sum(axis=0).astype(float)
        lx0=min(c['x0'] for c in cs); lx1=max(c['x1'] for c in cs)
        # candidate gaps inside [lx0,lx1]
        runs=[(s,e) for (s,e) in zero_runs(prof,0) if s>lx0 and e<lx1 and (e-s)>=2]
        runs.sort(key=lambda r:-(r[1]-r[0]))
        sep=sorted([ (s+e)//2 for (s,e) in runs[:nwords-1] ]) if nwords>1 else []
        wbounds=[lx0]+sep+[lx1]
        # assign comps to words by cx
        word_comps=[[] for _ in range(nwords)]
        for c in cs:
            wi=0
            while wi<nwords-1 and c['cx']>wbounds[wi+1]: wi+=1
            word_comps[wi].append(c)
        # split each word into syllables
        for wi,word in enumerate(words):
            wcs=sorted(word_comps[wi],key=lambda c:c['cx'])
            toks=list(word)
            if not wcs:
                for ch in toks: glyphs.append(dict(line=li,i=len(glyphs),ch=ch,img=None))
                continue
            wx0=min(c['x0'] for c in wcs); wx1=max(c['x1'] for c in wcs)
            # pull trailing period: tiny, low, rightmost component
            has_period = toks[-1]=='.'
            period_comp=None
            core=wcs
            if has_period:
                # period = small area component sitting low and near right
                cand=[c for c in wcs if c['area']<60 and c['cy']>(ly0+0.55*(ly1-ly0))]
                if cand:
                    period_comp=max(cand,key=lambda c:c['cx'])
                    core=[c for c in wcs if c is not period_comp]
                synt=toks[:-1]
            else:
                synt=toks
            Ns=len(synt)
            cx0=min(c['x0'] for c in core) if core else wx0
            cx1=max(c['x1'] for c in core) if core else wx1
            # even split of core x-range into Ns, snap to local minima of prof
            cell=(cx1-cx0)/max(1,Ns)
            sylb=[cx0]
            for k in range(1,Ns):
                g=int(cx0+k*cell); win=max(2,int(cell*0.30))
                a=max(cx0+1,g-win); b=min(cx1-1,g+win)
                seg=prof[a:b+1]
                sylb.append(a+int(np.argmin(seg)) if len(seg) else g)
            sylb.append(cx1)
            for k in range(1,len(sylb)):
                if sylb[k]<=sylb[k-1]: sylb[k]=sylb[k-1]+1
            # assign core comps to syllable cells
            cell_comps=[[] for _ in range(Ns)]
            for c in core:
                si=0
                while si<Ns-1 and c['cx']>sylb[si+1]: si+=1
                cell_comps[si].append(c)
            for si,ch in enumerate(synt):
                members=cell_comps[si]
                glyphs.append(make_glyph(li,len(glyphs),ch,members,lab))
            if has_period:
                glyphs.append(make_glyph(li,len(glyphs),'.',[period_comp] if period_comp else [],lab))
    # baseline per line
    for li in range(8):
        gl=[g for g in glyphs if g['line']==li and g['img'] is not None and g['ch']!='.']
        if not gl: continue
        base=int(np.median([g['gy1'] for g in gl]))
        base=max(g['gy1'] for g in gl)
        topcap=min(g['gy0'] for g in gl)
        for g in glyphs:
            if g['line']==li: g['base']=base; g['topcap']=topcap
    save_and_sheets(glyphs)

def make_glyph(li,idx,ch,members,lab):
    members=[m for m in members if m is not None]
    if not members:
        return dict(line=li,i=idx,ch=ch,img=None)
    gx0=min(m['x0'] for m in members); gx1=max(m['x1'] for m in members)
    gy0=min(m['y0'] for m in members); gy1=max(m['y1'] for m in members)
    sub=np.zeros((gy1-gy0,gx1-gx0),bool)
    for m in members:
        yy,xx=np.where(lab[m['y0']:m['y1'],m['x0']:m['x1']]==m['lbl'])
        sub[m['y0']-gy0+yy,m['x0']-gx0+xx]=True
    return dict(line=li,i=idx,ch=ch,img=(~sub*255).astype(np.uint8),
                w=int(gx1-gx0),h=int(gy1-gy0),gx0=int(gx0),gy0=int(gy0),gy1=int(gy1))

def save_and_sheets(glyphs):
    for g in glyphs:
        g['key']=f"L{g['line']}_{g['i']:03d}_{ord(g['ch']):04X}"
        if g['img'] is not None:
            Image.fromarray(g['img']).save(os.path.join(GLY,g['key']+".png"))
    json.dump([{k:v for k,v in g.items() if k!='img'} for g in glyphs],
              open(os.path.join(ROOT,"glyphs.json"),"w"),ensure_ascii=False)
    nf=ImageFont.truetype("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",15)
    by={}
    for g in glyphs: by.setdefault(g['line'],[]).append(g)
    CELL=74
    for li,gs in sorted(by.items()):
        sheet=Image.new("RGB",(len(gs)*CELL,CELL+24),(255,255,255)); d=ImageDraw.Draw(sheet)
        for j,g in enumerate(gs):
            x=j*CELL; d.rectangle([x,0,x+CELL-1,CELL+23],outline=(210,210,210))
            if g['img'] is not None:
                gi=Image.fromarray(g['img']); s=min((CELL-10)/gi.width,(CELL-10)/gi.height)
                gi=gi.resize((max(1,int(gi.width*s)),max(1,int(gi.height*s))))
                sheet.paste(gi,(x+(CELL-gi.width)//2,4+(CELL-10-gi.height)//2))
            d.text((x+3,CELL+3),f"{g['ch']}",fill=(200,0,0),font=nf)
        sheet.save(os.path.join(DBG,f"sheet_L{li}.png"))
    print("glyphs",len(glyphs),"empty",sum(1 for g in glyphs if g['img'] is None))

if __name__=="__main__":
    main()
