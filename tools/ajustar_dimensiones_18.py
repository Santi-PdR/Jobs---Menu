#!/usr/bin/env python3
from pathlib import Path
p=Path('src/main/java/com/santipdr/jobsmenu/client/scene/Nivel.java')
s=p.read_text(encoding='utf-8')
for n in (15,16,17):
    s=s.replace(f'new PlantaImagen("nivel{n}.png", 256, 144, {n})', f'new PlantaImagen("nivel{n}.png", 192, 108, {n})')
p.write_text(s,encoding='utf-8')
