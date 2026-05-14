#!/usr/bin/env bash
# One-shot downloader for Bible verse / scripture / typography card references.
# Sources: pexels.com (CC0 license) and unsplash.com (Unsplash License).

set -u
cd "$(dirname "$0")"

URLS=(
  # ─ Pexels: bible verse ─
  "https://images.pexels.com/photos/34497278/pexels-photo-34497278.jpeg"
  "https://images.pexels.com/photos/33866280/pexels-photo-33866280.jpeg"
  "https://images.pexels.com/photos/4747529/pexels-photo-4747529.jpeg"
  "https://images.pexels.com/photos/4567399/pexels-photo-4567399.jpeg"
  "https://images.pexels.com/photos/1771219/pexels-photo-1771219.jpeg"
  "https://images.pexels.com/photos/5685161/pexels-photo-5685161.jpeg"
  "https://images.pexels.com/photos/6860840/pexels-photo-6860840.jpeg"
  "https://images.pexels.com/photos/4654195/pexels-photo-4654195.jpeg"
  "https://images.pexels.com/photos/8383409/pexels-photo-8383409.jpeg"
  "https://images.pexels.com/photos/8383998/pexels-photo-8383998.jpeg"
  "https://images.pexels.com/photos/8383443/pexels-photo-8383443.jpeg"
  "https://images.pexels.com/photos/8735609/pexels-photo-8735609.jpeg"
  "https://images.pexels.com/photos/35644072/pexels-photo-35644072.jpeg"
  "https://images.pexels.com/photos/8383494/pexels-photo-8383494.jpeg"
  "https://images.pexels.com/photos/4747535/pexels-photo-4747535.jpeg"
  "https://images.pexels.com/photos/9042047/pexels-photo-9042047.jpeg"
  "https://images.pexels.com/photos/33657892/pexels-photo-33657892.jpeg"
  "https://images.pexels.com/photos/15203126/pexels-photo-15203126.jpeg"
  "https://images.pexels.com/photos/6860482/pexels-photo-6860482.jpeg"
  "https://images.pexels.com/photos/5199818/pexels-photo-5199818.jpeg"
  "https://images.pexels.com/photos/895449/pexels-photo-895449.jpeg"
  "https://images.pexels.com/photos/5789202/pexels-photo-5789202.jpeg"
  "https://images.pexels.com/photos/5199810/pexels-photo-5199810.jpeg"
  "https://images.pexels.com/photos/32506376/pexels-photo-32506376.jpeg"
  # ─ Pexels: scripture ─
  "https://images.pexels.com/photos/6860489/pexels-photo-6860489.jpeg"
  "https://images.pexels.com/photos/37084461/pexels-photo-37084461.jpeg"
  "https://images.pexels.com/photos/37084464/pexels-photo-37084464.jpeg"
  "https://images.pexels.com/photos/8275630/pexels-photo-8275630.jpeg"
  "https://images.pexels.com/photos/8735603/pexels-photo-8735603.jpeg"
  "https://images.pexels.com/photos/5257932/pexels-photo-5257932.jpeg"
  "https://images.pexels.com/photos/6860848/pexels-photo-6860848.jpeg"
  "https://images.pexels.com/photos/20430380/pexels-photo-20430380.jpeg"
  "https://images.pexels.com/photos/37100086/pexels-photo-37100086.jpeg"
  "https://images.pexels.com/photos/5257933/pexels-photo-5257933.jpeg"
  "https://images.pexels.com/photos/5199750/pexels-photo-5199750.jpeg"
  "https://images.pexels.com/photos/6860831/pexels-photo-6860831.jpeg"
  "https://images.pexels.com/photos/8268313/pexels-photo-8268313.jpeg"
  "https://images.pexels.com/photos/5243956/pexels-photo-5243956.jpeg"
  "https://images.pexels.com/photos/6860858/pexels-photo-6860858.jpeg"
  "https://images.pexels.com/photos/23021404/pexels-photo-23021404.jpeg"
  "https://images.pexels.com/photos/7969579/pexels-photo-7969579.jpeg"
  "https://images.pexels.com/photos/31903107/pexels-photo-31903107.jpeg"
  "https://images.pexels.com/photos/7829505/pexels-photo-7829505.jpeg"
  # ─ Pexels: typography quote ─
  "https://images.pexels.com/photos/6659563/pexels-photo-6659563.jpeg"
  "https://images.pexels.com/photos/35891295/pexels-photo-35891295.jpeg"
  "https://images.pexels.com/photos/31994880/pexels-photo-31994880.jpeg"
  "https://images.pexels.com/photos/6185653/pexels-photo-6185653.jpeg"
  "https://images.pexels.com/photos/5767028/pexels-photo-5767028.jpeg"
  "https://images.pexels.com/photos/6185656/pexels-photo-6185656.jpeg"
  "https://images.pexels.com/photos/3309775/pexels-photo-3309775.jpeg"
  "https://images.pexels.com/photos/3280211/pexels-photo-3280211.jpeg"
  "https://images.pexels.com/photos/5981822/pexels-photo-5981822.jpeg"
  "https://images.pexels.com/photos/6005010/pexels-photo-6005010.jpeg"
  "https://images.pexels.com/photos/6185632/pexels-photo-6185632.jpeg"
  "https://images.pexels.com/photos/6431877/pexels-photo-6431877.jpeg"
  "https://images.pexels.com/photos/6185549/pexels-photo-6185549.jpeg"
  "https://images.pexels.com/photos/5238670/pexels-photo-5238670.jpeg"
  "https://images.pexels.com/photos/5981704/pexels-photo-5981704.jpeg"
  "https://images.pexels.com/photos/5981783/pexels-photo-5981783.jpeg"
  "https://images.pexels.com/photos/5246429/pexels-photo-5246429.jpeg"
  "https://images.pexels.com/photos/6185233/pexels-photo-6185233.jpeg"
  "https://images.pexels.com/photos/6005009/pexels-photo-6005009.jpeg"
  "https://images.pexels.com/photos/6185624/pexels-photo-6185624.jpeg"
  "https://images.pexels.com/photos/7661187/pexels-photo-7661187.jpeg"
  "https://images.pexels.com/photos/5707491/pexels-photo-5707491.jpeg"
  "https://images.pexels.com/photos/6005495/pexels-photo-6005495.jpeg"
  "https://images.pexels.com/photos/6005515/pexels-photo-6005515.jpeg"
  # ─ Pexels: christian art ─
  "https://images.pexels.com/photos/35489117/pexels-photo-35489117.jpeg"
  "https://images.pexels.com/photos/18146481/pexels-photo-18146481.jpeg"
  "https://images.pexels.com/photos/10853548/pexels-photo-10853548.jpeg"
  "https://images.pexels.com/photos/36915428/pexels-photo-36915428.jpeg"
  "https://images.pexels.com/photos/19085090/pexels-photo-19085090.jpeg"
  "https://images.pexels.com/photos/19354876/pexels-photo-19354876.jpeg"
  "https://images.pexels.com/photos/13629838/pexels-photo-13629838.jpeg"
  "https://images.pexels.com/photos/37484468/pexels-photo-37484468.jpeg"
  "https://images.pexels.com/photos/13985825/pexels-photo-13985825.jpeg"
  "https://images.pexels.com/photos/29806083/pexels-photo-29806083.jpeg"
  "https://images.pexels.com/photos/35561184/pexels-photo-35561184.jpeg"
  "https://images.pexels.com/photos/18577782/pexels-photo-18577782.jpeg"
  "https://images.pexels.com/photos/17951423/pexels-photo-17951423.jpeg"
  "https://images.pexels.com/photos/18506917/pexels-photo-18506917.jpeg"
  "https://images.pexels.com/photos/5641506/pexels-photo-5641506.jpeg"
  "https://images.pexels.com/photos/7219104/pexels-photo-7219104.jpeg"
  "https://images.pexels.com/photos/208216/pexels-photo-208216.jpeg"
  "https://images.pexels.com/photos/8659932/pexels-photo-8659932.jpeg"
  "https://images.pexels.com/photos/24734796/pexels-photo-24734796.jpeg"
  "https://images.pexels.com/photos/30489160/pexels-photo-30489160.jpeg"
  "https://images.pexels.com/photos/29002735/pexels-photo-29002735.jpeg"
  "https://images.pexels.com/photos/34894703/pexels-photo-34894703.jpeg"
  "https://images.pexels.com/photos/35620383/pexels-photo-35620383.jpeg"
  "https://images.pexels.com/photos/12215315/pexels-photo-12215315.jpeg"
  # ─ Unsplash: bible verse ─
  "https://images.unsplash.com/photo-1535440216424-0e374e613ee5?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1537806817607-45d08e8291bc?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1620048496289-82fed83ed559?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1620981956707-1cc51efa5731?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1624472603343-98d661edfe9e?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1612350275777-e86877ce74ad?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1654973172085-0728ab7730bf?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1654617850887-fedbeb6b8433?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1601467700022-19b4260b2594?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1512099693625-6bc69d03a340?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1518406479616-cd3f1cde0a50?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1536126750180-3c7d59643f99?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1616963299323-8123461ee86c?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1515666991427-9b0f67becfa1?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1593193583588-66f1dcc0625b?fm=jpg&q=80&w=2000"
  # ─ Unsplash: scripture card ─
  "https://images.unsplash.com/photo-1620048519803-5b80d02b0127?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1645342854904-6d8946353b8f?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1625472067906-735275d56192?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1524776393716-04e8d8eb2c6b?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1631284184314-5575ea3b5e00?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1654617832494-151c25760e19?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1645342953496-751cf03113db?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1625472095277-31f492320974?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1625472049587-68137ef7b744?fm=jpg&q=80&w=2000"
  "https://images.unsplash.com/photo-1733126204538-edca39479221?fm=jpg&q=80&w=2000"
)

ok=0
fail=0
i=0
for url in "${URLS[@]}"; do
  i=$((i+1))
  # Derive a stable filename from the URL
  base=$(echo "$url" | sed -E 's|https://images\.||; s|[^A-Za-z0-9_.-]|_|g' | head -c 80)
  out=$(printf "%03d_%s.jpg" "$i" "$base")
  if [[ -s "$out" ]]; then
    echo "skip  $out (exists)"
    ok=$((ok+1))
    continue
  fi
  if curl -fsSL -A "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36" \
      --max-time 30 -o "$out" "$url"; then
    sz=$(stat -c%s "$out" 2>/dev/null || echo 0)
    if [[ "$sz" -lt 1024 ]]; then
      echo "small $out ($sz B) — removing"
      rm -f "$out"
      fail=$((fail+1))
    else
      echo "ok    $out ($sz B)"
      ok=$((ok+1))
    fi
  else
    echo "fail  $out"
    rm -f "$out"
    fail=$((fail+1))
  fi
done

echo "──────────────"
echo "downloaded: $ok / failed: $fail / total attempted: ${#URLS[@]}"
