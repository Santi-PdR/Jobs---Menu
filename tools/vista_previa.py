#!/usr/bin/env python3
"""Hoja de dirección visual V2 para los diez fondos de Jobs Menu 1.0.1.

No emula GuiGraphics. Comprueba que composición, masa, foco y planos de cada
recinto sean reconocibles antes de iniciar Minecraft. Requiere Pillow.
"""
from __future__ import annotations

import argparse
import math
import random
from pathlib import Path
from PIL import Image, ImageDraw

W, H = 480, 270
PALETAS = [
    ((34, 30, 15), (146, 126, 49), (238, 220, 116)),
    ((17, 21, 23), (82, 88, 83), (222, 232, 238)),
    ((17, 10, 5), (78, 47, 24), (255, 177, 87)),
    ((6, 20, 23), (75, 137, 139), (220, 243, 239)),
    ((10, 6, 3), (91, 62, 35), (255, 190, 103)),
    ((16, 12, 7), (79, 58, 35), (161, 193, 113)),
    ((18, 24, 13), (66, 84, 47), (218, 230, 190)),
    ((5, 7, 9), (54, 61, 68), (255, 219, 149)),
    ((3, 7, 12), (31, 48, 64), (255, 198, 112)),
    ((9, 7, 17), (57, 53, 80), (229, 197, 113)),
]


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def canvas(i):
    dark, mid, light = PALETAS[i]
    im = Image.new("RGB", (W, H), dark)
    d = ImageDraw.Draw(im, "RGB")
    for y in range(H):
        t = y / max(1, H - 1)
        d.line((0, y, W, y), fill=mix(mid, dark, .25 + t * .61))
    rng = random.Random(9100 + i)
    for _ in range(190):
        x, y = rng.randrange(W), rng.randrange(H)
        r = rng.randrange(1, 8)
        c = mix(dark, mid, rng.random() * .30)
        d.rectangle((x, y, x + r, y + max(1, r // 3)), fill=c)
    return im, d, dark, mid, light


def glow(im, cx, cy, radius, color, strength=.22):
    layer = Image.new("RGBA", im.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer, "RGBA")
    for r in range(radius, 2, -4):
        a = round(255 * strength * (1 - r / radius) ** 1.6)
        d.ellipse((cx-r, cy-r, cx+r, cy+r), fill=(*color, a))
    im.alpha_composite(layer) if im.mode == "RGBA" else im.paste(layer, mask=layer)


def sala():
    im, d, dark, mid, light = canvas(0)
    d.rectangle((0, 0, W, 48), fill=mix(mid, light, .28))
    d.polygon([(0, 0), (160, 0), (202, 66), (91, 66)], fill=dark)
    for x in range(94, W, 48): d.line((x, 0, x + 24, 48), fill=mix(mid, dark, .45), width=2)
    d.rectangle((0, 92, W, 111), fill=(70, 82, 42))
    d.rectangle((67, 65, 163, 184), fill=(72, 64, 33)); d.rectangle((74, 72, 156, 91), fill=(201, 193, 143))
    d.ellipse((188, 63, 222, 97), fill=(206, 195, 139), outline=dark, width=4)
    d.rectangle((237, 57, W, 151), fill=dark)
    for x in range(240, W, 60): d.rectangle((x+3, 60, x+56, 132), fill=(58, 75, 61), outline=(66, 59, 30), width=3)
    d.rectangle((223, 132, W, 185), fill=(76, 56, 25)); d.rectangle((217, 130, W, 140), fill=(48, 42, 21))
    d.polygon([(0, 181), (W, 181), (W, H), (0, H)], fill=(80, 69, 32))
    for i in range(6):
        t=i/5; x=222-round(t*145); y=190+round(t*55)
        d.line((x, y-25-round(t*10), x, y), fill=(58, 52, 28), width=3)
        if i: d.line((px, py-22, x, y-25-round(t*10)), fill=(58, 52, 28), width=2)
        px, py=x, y
    d.polygon([(0, 222), (205, 222), (248, H), (0, H)], fill=(48, 38, 17))
    d.polygon([(42, 216), (150, 211), (181, 229), (36, 234)], fill=(216, 207, 161))
    return im


def nave():
    im, d, dark, mid, light = canvas(1)
    d.rectangle((77, 48, 436, 178), fill=(87, 91, 84))
    for i in range(5):
        x=96+i*66; d.rectangle((x, 92, x+49, 178), fill=dark)
        for y in range(100, 177, 12): d.line((x, y, x+49, y), fill=(69, 74, 69))
    d.polygon([(60, 166), (450, 166), (W, H), (0, H)], fill=(63, 67, 64))
    for i in range(4): d.line((180+i*47, 166, -20+i*166, H), fill=(107, 110, 105), width=3)
    colors=((64,79,71),(77,83,91),(86,68,52))
    for side in (0,1):
        x0,x1=(0,142) if side==0 else (345,W)
        floors=4 if side==0 else 3
        for p in range(floors):
            y1=220-p*45; y0=y1-37; dx=(-p*5 if side==0 else p*4)
            d.rectangle((x0+dx,y0,x1+dx,y1), fill=colors[p%3], outline=(45,49,47), width=3)
            for x in range(x0+dx+8,x1+dx,17): d.line((x,y0+2,x,y1-2),fill=(114,96,73))
    d.rectangle((0, 35, W, 59), fill=(55, 61, 59), outline=(102, 106, 99), width=3)
    for x in range(20,W,45): d.polygon([(x,40),(x+9,40),(x+15,53),(x+6,53)],fill=dark)
    d.rectangle((285,59,326,84),fill=(65,70,66)); d.line((305,84,290,155),fill=(132,133,125),width=2)
    d.polygon([(0,238),(139,238),(160,H),(0,H)],fill=(80,54,28))
    return im


def servicio():
    im, d, dark, mid, light = canvas(2)
    for x in range(0,W,50): d.rectangle((x,0,x+47,H),fill=mix(mid,dark,.35),outline=(83,49,24),width=2)
    glow(im, 325, 133, 95, light, .16)
    d.ellipse((210,22,455,260),fill=(71,42,23),outline=(103,62,30),width=15)
    for x in range(235,440,29): d.line((x,45,x,235),fill=(122,73,34),width=2)
    d.ellipse((295,102,367,174),fill=dark,outline=(111,69,34),width=9)
    for i in range(12):
        a=i*math.pi/6; x=332+math.cos(a)*103; y=141+math.sin(a)*100
        d.ellipse((x-3,y-3,x+3,y+3),fill=(154,105,53))
    for i in range(5):
        y=26+i*24; x=124+i*13; c=(133,84,38) if i==2 else (76,62,45)
        d.line((-10,y,x,y,x,94+i*21,225,112+i*11),fill=c,width=5+i%2)
    d.rectangle((0,184,W,207),fill=(48,45,36)); d.line((0,157,W,157),fill=(83,72,53),width=4)
    for x in range(0,W,45): d.line((x,157,x,200),fill=(83,72,53),width=3)
    for x in (62,122): d.ellipse((x-17,132,x+17,166),fill=(210,191,139),outline=(126,76,35),width=4)
    d.ellipse((27,205,97,275),fill=dark,outline=(102,67,36),width=9)
    for i in range(6):
        a=i*math.pi/3; d.line((62,240,62+math.cos(a)*31,240+math.sin(a)*31),fill=(105,70,38),width=3)
    return im


def natatorio():
    im, d, dark, mid, light = canvas(3)
    d.rectangle((0,0,W,146),fill=(139,177,175))
    for x in range(0,W,18): d.line((x,0,x,146),fill=(79,116,115))
    for y in range(0,146,18): d.line((0,y,W,y),fill=(79,116,115))
    for i in range(6):
        x=38+i*70; d.rectangle((x,20,x+48,78),fill=(43,72,76),outline=(60,84,85),width=3)
        d.polygon([(x,78),(x+48,78),(x+90,213),(x-32,213)],fill=(187,230,226))
    for i in range(4): d.rectangle((302+i*10,102+i*15,W,113+i*15),fill=(172,194,191))
    d.polygon([(54,143),(402,143),(444,H),(-65,H)],fill=(32,92,99))
    for y in range(150,H,14): d.line((0,y,435,y),fill=(63,131,135),width=2)
    for i in range(5): d.line((100+i*57,145,25+i*84,H),fill=(173,219,214),width=2)
    d.rectangle((405,43,424,222),fill=(100,118,112))
    for i in range(3):
        y=74+i*38; d.rectangle((335+i*8,y,435,y+10),fill=(200,218,213)); d.line((415,y,350+i*8,y+30),fill=(83,104,99),width=3)
    d.line((-20,211,185,246),fill=(77,88,85),width=8)
    for i in range(4): d.line((i*58-10,208+i*11,i*58-10,H),fill=(77,88,85),width=4)
    return im


def cripta():
    im, d, dark, mid, light = canvas(4); cx,cy=255,104
    d.ellipse((53,-89,457,313),fill=(111,75,42),outline=(64,42,23),width=11)
    d.ellipse((91,-51,419,275),fill=dark,outline=(72,49,27),width=10)
    for i in range(12):
        a=math.pi+i*math.pi/11; d.line((cx,cy,cx+math.cos(a)*200,cy+math.sin(a)*200),fill=(102,70,39),width=4)
    d.ellipse((220,28,290,98),fill=dark,outline=(116,80,43),width=8)
    for i in range(7):
        x=62+i*60; r=30-abs(3-i)*2
        d.pieslice((x-r,84-r,x+r,202),180,360,fill=(118,80,45)); d.rectangle((x-r,114,x+r,202),fill=dark)
    d.polygon([(0,188),(W,188),(W,H),(0,H)],fill=(77,51,28))
    for i in range(5): d.line((255-45-i*35,194+i*14,255+45+i*35,194+i*14),fill=(126,87,45),width=2)
    d.polygon([(215,194),(295,194),(309,216),(201,216)],fill=(82,55,29)); d.rectangle((225,157,285,194),fill=dark)
    for x in (230,250,270,290): glow(im,x,153,25,light,.18); d.rectangle((x-2,151,x+2,164),fill=light)
    d.line((-20,200,145,285),fill=(45,29,16),width=34)
    return im


def biblioteca():
    im, d, dark, mid, light = canvas(5); cx,cy=230,164
    d.ellipse((-30,-115,490,405),fill=(77,53,31)); d.ellipse((148,82,312,246),fill=dark)
    for floor in range(3):
        y=25+floor*55
        for i in range(11):
            x=-18+i*49; inset=abs((x+24)-cx)//17
            d.rectangle((x,y+inset,x+44,y+48),fill=(75,51,29),outline=(103,72,39),width=2)
            for k in range(5): d.rectangle((x+4+k*8,y+27+inset,x+9+k*8,y+46),fill=(57+k*7,38+k*4,28))
        d.line((0,y+49,W,y+49),fill=(135,106,57),width=5)
        for x in range(8,W,15): d.line((x,y+33,x,y+49),fill=(124,96,54),width=2)
    d.ellipse((155,183,305,295),fill=(111,82,43)); d.ellipse((177,199,283,281),fill=dark)
    for i in range(14):
        a=i*.48; x=cx+math.cos(a)*64; y=213+math.sin(a)*18-i*3
        d.line((x-16,y,x+16,y),fill=(145,112,58),width=3)
    glow(im,cx,109,62,(145,180,112),.16); d.line((cx,0,cx,109),fill=(135,108,61),width=2); d.rectangle((211,109,249,119),fill=(101,137,80))
    d.polygon([(344,211),(W,211),(W,H),(298,H)],fill=(45,31,18)); d.polygon([(350,200),(450,194),(W,230),(326,232)],fill=(219,210,166))
    return im


def invernadero():
    im, d, dark, mid, light = canvas(6); cx,base=205,166; r=218
    d.ellipse((cx-r,base-r,cx+r,base+r),fill=(95,124,91),outline=(48,59,39),width=7)
    d.ellipse((cx-r+8,base-r+8,cx+r-8,base+r-8),fill=(83,113,91))
    for i in range(11):
        a=math.pi+i*math.pi/10; d.line((cx,base,cx+math.cos(a)*r,base+math.sin(a)*r),fill=(46,57,39),width=3)
    for q in range(1,5): d.ellipse((cx-r*q/5,base-r*q/5,cx+r*q/5,base+r*q/5),outline=(54,67,45),width=2)
    d.polygon([(270,0),(383,0),(345,58),(242,48)],fill=dark); d.polygon([(271,0),(383,0),(390,177),(210,177)],fill=(206,224,188))
    d.polygon([(0,166),(W,166),(W,H),(0,H)],fill=(69,75,43))
    d.line((218,213,236,58),fill=(54,40,24),width=28); d.line((230,128,115,72),fill=(54,40,24),width=16); d.line((232,108,350,58),fill=(54,40,24),width=13)
    rng=random.Random(44)
    for _ in range(45):
        x=rng.randrange(70,410); y=rng.randrange(34,151); rr=rng.randrange(4,12)
        d.ellipse((x-rr,y-rr,x+rr,y+rr),fill=(34+rng.randrange(28),67+rng.randrange(35),29))
    for x in range(25,445,42): d.line((220,211,x,H),fill=(57,43,25),width=6)
    return im


def catacumba():
    im, d, dark, mid, light = canvas(7)
    for side in (0,1):
        x0,x1=(0,150) if side==0 else (350,W)
        for row in range(4):
            y=28+row*43
            for col in range(3):
                x=x0+8+col*46; d.rectangle((x,y,x+37,y+31),fill=dark,outline=(79,86,92),width=3)
    landings=((118,101,92),(268,131,76),(389,156,58))
    for idx,(cx,top,r) in enumerate(landings):
        d.pieslice((cx-r,top-r,cx+r,top+r*2),180,360,fill=(84-idx*8,91-idx*8,98-idx*8)); d.rectangle((cx-r,top,cx+r,top+r),fill=dark)
        if idx<2:
            nx,ny,_=landings[idx+1]
            for s in range(6):
                t=s/5; x=cx+(nx-cx)*t; y=top+r+(ny-top-r)*t+s*6
                d.rectangle((x-33+s*3,y,x+33-s*3,y+6),fill=(62,67,72))
    glow(im,184,105,35,light,.16); d.line((184,0,184,105),fill=(107,100,78),width=2); d.rectangle((180,105,188,121),fill=light)
    d.line((-20,0,44,H),fill=(33,37,41),width=38); d.line((W+15,28,445,H),fill=(31,35,39),width=34)
    return im


def cisterna():
    im, d, dark, mid, light = canvas(8); cx,cy=250,159; r=140
    d.ellipse((cx-r,cy-r,cx+r,cy+r),fill=(52,67,81))
    for i in range(6):
        rr=round(r*(1-i*.115)); yy=cy+i*13
        d.ellipse((cx-rr,yy-rr*.55,cx+rr,yy+rr*.55),fill=dark,outline=mix(mid,dark,i*.12),width=10)
    for i in range(10):
        a=i*math.pi/5; d.line((cx+math.cos(a)*r*.92,cy+math.sin(a)*r*.5,cx+math.cos(a)*r*.34,cy+45+math.sin(a)*r*.17),fill=(54,70,85),width=8)
    d.ellipse((205,198,295,238),fill=(11,25,36))
    for y in range(205,235,6): d.line((220,y,280,y),fill=(57,89,105),width=2)
    d.line((92,0,143,H),fill=(64,70,70),width=18)
    d.line((382,20,330,240),fill=(100,106,102),width=3); d.line((399,20,347,240),fill=(100,106,102),width=3)
    for i in range(15):
        t=i/14; x=382-52*t; y=20+220*t; d.line((x,y,x+17,y),fill=(100,106,102),width=2)
    d.polygon([(0,210),(136,210),(205,H),(0,H)],fill=(43,47,48)); d.polygon([(375,221),(W,202),(W,H),(335,H)],fill=(43,47,48))
    return im


def trono():
    im, d, dark, mid, light = canvas(9); cx=260
    d.ellipse((cx-78,-32,cx+78,124),fill=(22,19,36),outline=(104,98,119),width=16)
    for i in range(8):
        a=i*math.pi/4; d.line((cx+math.cos(a)*55,46+math.sin(a)*55,cx+math.cos(a)*77,46+math.sin(a)*77),fill=(151,127,65),width=4)
    d.polygon([(242,90),(278,90),(350,224),(168,224)],fill=(109,96,70))
    for x0,x1 in ((0,150),(350,W)):
        d.rectangle((x0,27,x1,202),fill=(72,69,91))
        for k in range(3):
            x=x0+k*55 if x0==0 else x1-k*55; d.line((x,27,x+(28 if x0==0 else -28),202),fill=(47,44,62),width=13)
    d.polygon([(60,170),(420,170),(W,H),(0,H)],fill=dark)
    d.polygon([(0,207),(126,207),(186,H),(0,H)],fill=(50,47,65)); d.polygon([(378,202),(W,202),(W,H),(334,H)],fill=(50,47,65))
    d.polygon([(205,171),(315,171),(340,202),(180,202)],fill=(83,76,83))
    for i in range(4): d.rectangle((202-i*10,177+i*6,318+i*10,182+i*6),fill=(104,91,74))
    d.rectangle((241,112,279,171),fill=(150,126,62)); d.rectangle((248,121,272,166),fill=dark)
    d.line((241,112,260,92,279,112),fill=(163,138,69),width=4)
    for side in (-1,1):
        x=cx+side*137; d.line((x,0,x,43),fill=(140,116,61),width=3)
        d.polygon([(x-17,43),(x+17,43),(x+10+side*5,139),(x-12+side*4,139)],fill=(42,34,62))
    d.line((-10,65,36,H),fill=(34,31,49),width=42); d.line((W+10,48,452,H),fill=(34,31,49),width=38)
    return im


SCENES = [sala, nave, servicio, natatorio, cripta, biblioteca,
          invernadero, catacumba, cisterna, trono]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("salida", nargs="?", default="docs/vista_previa.png")
    ap.add_argument("--nivel", type=int)
    args = ap.parse_args()
    if args.nivel is not None:
        image = SCENES[args.nivel % 10]()
        Path(args.salida).parent.mkdir(parents=True, exist_ok=True)
        image.save(args.salida)
        return
    sheet = Image.new("RGB", (W * 2, H * 5))
    for i, fn in enumerate(SCENES):
        sheet.paste(fn(), ((i % 2) * W, (i // 2) * H))
    Path(args.salida).parent.mkdir(parents=True, exist_ok=True)
    sheet.save(args.salida)
    print(f"{args.salida}: {sheet.width}x{sheet.height}, 10 escenas V2")


if __name__ == "__main__":
    main()
