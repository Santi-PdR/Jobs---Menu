#!/usr/bin/env python3
"""Hoja de direccion de arte para los diez fondos de Jobs Menu 1.0.

No emula GuiGraphics pixel por pixel. Sirve para comprobar a simple vista que
cada composicion conserva su silueta, foco y reparto foreground/mid/background.
Requiere Pillow: python -m pip install pillow
"""
from __future__ import annotations
import argparse
from pathlib import Path
from PIL import Image, ImageDraw

W, H = 480, 270
P = [
    ((37, 33, 13), (150, 132, 47), (234, 217, 112)),
    ((23, 27, 29), (83, 88, 82), (222, 232, 238)),
    ((18, 11, 5), (74, 49, 26), (255, 179, 90)),
    ((8, 23, 26), (73, 133, 135), (220, 242, 239)),
    ((10, 6, 3), (91, 63, 34), (255, 190, 103)),
    ((18, 14, 8), (82, 61, 37), (168, 193, 112)),
    ((20, 27, 15), (68, 84, 48), (217, 229, 190)),
    ((6, 8, 10), (55, 62, 68), (255, 218, 148)),
    ((4, 8, 13), (31, 48, 64), (255, 198, 112)),
    ((10, 8, 18), (55, 52, 78), (226, 194, 112)),
]

def canvas(i: int):
    bg, mid, light = P[i]
    im = Image.new("RGB", (W, H), bg)
    return im, ImageDraw.Draw(im), bg, mid, light

def glow(d, x, y, r, color):
    for q in range(r, 2, -4):
        a = int(10 + 55 * (1 - q / r))
        c = tuple(min(255, int(v * a / 90)) for v in color)
        d.ellipse((x-q, y-q, x+q, y+q), fill=c)

def sala():
    im,d,b,m,l=canvas(0); fy=122
    d.polygon([(0,0),(W,0),(330,fy),(126,fy)],fill=(128,116,51))
    d.polygon([(0,H),(W,H),(330,fy),(126,fy)],fill=(73,62,28))
    d.rectangle((126,50,330,157),fill=(139,124,48))
    for x in (174,280): d.rectangle((x,88,x+24,157),fill=b)
    d.rectangle((150,148,265,169),fill=(76,64,25))
    for k in range(5):
        y=21+k*17; d.line((45+k*28,y,355-k*10,y+35),fill=l,width=3)
    d.polygon([(0,207),(236,207),(270,H),(0,H)],fill=(49,41,17))
    d.rectangle((402,91,W,H),fill=(45,46,37))
    return im

def nave():
    im,d,b,m,l=canvas(1); fy=174
    d.rectangle((125,85,355,fy),fill=(75,80,75)); d.rectangle((175,108,305,fy),fill=(8,11,12))
    for k in range(6):
        y=20+k*23; span=235-k*22
        d.line((W//2-span,y,W//2,y-35),fill=(93,99,95),width=4)
        d.line((W//2,y-35,W//2+span,y),fill=(93,99,95),width=4)
    for x in (38,91,389,442): d.rectangle((x,52,x+12,H),fill=(70,75,70))
    d.rectangle((0,47,W,58),fill=(48,52,49)); d.rectangle((296,58,330,78),fill=(58,62,57))
    d.line((313,78,313,160),fill=(120,121,111),width=2)
    return im

def servicio():
    im,d,b,m,l=canvas(2); d.rectangle((98,57,321,218),fill=(57,36,18)); d.rectangle((98,90,210,218),fill=(4,2,1))
    for k in range(5):
        y=18+k*14; d.line((-10,y,342,y+k*4),fill=(99,63,30),width=5+k%2)
        d.line((342,y+k*4,342,130+k*11),fill=(99,63,30),width=5+k%2)
    d.rectangle((369,70,W,230),fill=(51,35,24))
    for y in (103,141,179):
        d.ellipse((390,y-12,414,y+12),outline=(188,143,77),width=3)
    d.ellipse((45,158,116,229),outline=(103,66,31),width=8)
    d.line((45,194,116,194),fill=(103,66,31),width=4); d.line((80,158,80,229),fill=(103,66,31),width=4)
    glow(d,292,92,35,l)
    return im

def natatorio():
    im,d,b,m,l=canvas(3); water=(37,102,108); y=130
    for x in range(190,430,52): d.rectangle((x,25,x+31,91),fill=(42,75,77)); d.rectangle((x+3,28,x+28,88),fill=(148,194,190))
    for s in range(5): d.rectangle((0,y-s*12,120-s*10,y-s*12+10),fill=(166,193,190))
    d.polygon([(124,y),(460,y),(W,H),(35,H)],fill=water)
    for k in range(5): d.line((145+k*70,y,64+k*99,H),fill=(146,216,211),width=2)
    for q in range(9): d.line((124,y+8+q*15,462,y+8+q*15),fill=(92,162,164),width=1)
    d.line((372,106,354,223),fill=(213,233,230),width=4); d.line((395,106,377,223),fill=(213,233,230),width=4)
    return im

def cripta():
    im,d,b,m,l=canvas(4); cx=255
    d.rectangle((0,0,W,H),fill=(58,39,21))
    d.pieslice((135,25,375,265),180,360,fill=(120,83,45)); d.rectangle((135,145,375,H),fill=(120,83,45))
    d.pieslice((165,48,345,236),180,360,fill=b); d.rectangle((165,142,345,236),fill=b)
    for x in (65,445): d.rectangle((x-16,75,x+16,H),fill=(111,76,42))
    d.rectangle((210,195,300,216),fill=(78,52,28)); d.rectangle((197,216,313,225),fill=(54,36,20))
    for x in (90,404): glow(d,x,155,42,l); d.rectangle((x-4,155,x+4,170),fill=l)
    d.polygon([(0,220),(170,220),(198,H),(0,H)],fill=(39,26,14))
    return im

def biblioteca():
    im,d,b,m,l=canvas(5); d.rectangle((0,0,W,H),fill=(49,35,21))
    for x0,x1 in ((0,145),(335,W)):
        d.rectangle((x0,22,x1,H),fill=(66,45,25))
        for y in range(48,232,28):
            d.rectangle((x0,y,x1,y+4),fill=(94,66,36))
            for x in range(x0+4,x1-4,8): d.rectangle((x,y-18,x+5,y),fill=((90+x)%130+45,62,31))
    d.rectangle((0,111,W,117),fill=(88,61,34))
    for x in range(8,W,18): d.rectangle((x,96,x+2,111),fill=(95,67,38))
    d.rectangle((220,65,270,173),fill=(17,14,9))
    d.line((382,38,305,223),fill=(105,75,42),width=5)
    d.polygon([(110,216),(372,216),(430,H),(75,H)],fill=(54,38,23))
    return im

def invernadero():
    im,d,b,m,l=canvas(6); d.polygon([(0,108),(W//2,5),(W,108),(W,H),(0,H)],fill=(77,99,67))
    d.polygon([(0,108),(W//2,5),(W//2,108)],fill=(153,180,139)); d.polygon([(W//2,5),(W,108),(W//2,108)],fill=(133,165,119))
    d.line((0,108,W//2,5,W,108),fill=(46,55,35),width=5)
    for x in range(30,W,55): d.line((x,95,W//2,8),fill=(65,76,48),width=2)
    d.polygon([(210,135),(270,135),(350,H),(130,H)],fill=(83,78,48))
    for side in (range(0,195,14),range(285,W,14)):
        for x in side:
            h=28+(x*17)%70; d.line((x,H,x+((x%3)-1)*12,H-h),fill=(28,57,24),width=4)
            d.ellipse((x-5,H-h-5,x+8,H-h+8),fill=(42,77,34))
    return im

def catacumba():
    im,d,b,m,l=canvas(7); d.rectangle((0,0,W,H),fill=(40,45,50))
    d.pieslice((155,45,345,245),180,360,fill=(84,91,98)); d.rectangle((155,145,345,H),fill=(84,91,98))
    d.pieslice((184,70,316,220),180,360,fill=b); d.rectangle((184,145,316,220),fill=b)
    d.pieslice((15,110,125,240),180,360,fill=(72,78,83)); d.rectangle((15,175,125,240),fill=b)
    for x in (45,105,375,435):
        for y in (78,132,186): d.rectangle((x-15,y-18,x+15,y+18),fill=(12,15,17))
    glow(d,302,92,28,l); d.line((302,0,302,92),fill=(98,91,70),width=2)
    for k in range(18): x=270+(k*37)%205; y=220+(k*19)%50; d.rectangle((x,y-5-(k%8),x+7+k%5,y),fill=(70,76,80))
    return im

def cisterna():
    im,d,b,m,l=canvas(8); water=(8,19,29); d.rectangle((0,155,W,H),fill=water)
    for x,w in ((50,18),(126,13),(196,10),(284,10),(354,13),(432,18)):
        d.rectangle((x-w,0,x+w,225),fill=(52,68,82)); d.rectangle((x-w-3,0,x+w+3,8),fill=(71,86,98))
        d.line((x,190,x,H),fill=(41,65,78),width=max(2,w//3))
    d.polygon([(0,184),(100,184),(165,H),(0,H)],fill=(40,45,48)); d.line((0,154,122,220),fill=(97,104,107),width=3)
    for x in (210,285,360,425): glow(d,x,183,28,l); d.rectangle((x-4,181,x+4,184),fill=l)
    return im

def trono():
    im,d,b,m,l=canvas(9); cx=240; d.rectangle((0,0,W,H),fill=(42,39,60))
    d.pieslice((105,5,375,255),180,360,fill=(90,86,112)); d.rectangle((105,130,375,H),fill=(90,86,112))
    d.pieslice((140,38,340,224),180,360,fill=b); d.rectangle((140,130,340,224),fill=b)
    for x in (46,104,376,434): d.rectangle((x-12,28,x+12,H),fill=(70,67,91))
    for s in range(5): d.rectangle((cx-58-s*10,215+s*7,cx+58+s*10,221+s*7),fill=(76,70,91))
    d.rectangle((220,157,260,214),fill=(135,112,55)); d.rectangle((226,165,254,207),fill=(18,15,28))
    for p in range(-2,3): d.line((cx+p*11,157,cx+p*14,142-abs(p)*4),fill=l,width=3)
    d.polygon([(232,0),(248,0),(305,220),(175,220)],fill=(67,62,78))
    d.polygon([(0,104),(34,104),(34,H),(0,H)],fill=(34,31,48)); d.polygon([(445,59),(W,59),(W,H),(458,H)],fill=(34,31,48))
    return im

SCENES=[sala,nave,servicio,natatorio,cripta,biblioteca,invernadero,catacumba,cisterna,trono]

def main():
    ap=argparse.ArgumentParser(); ap.add_argument("salida",nargs="?",default="docs/vista_previa.png")
    ap.add_argument("--nivel",type=int); args=ap.parse_args()
    if args.nivel is not None:
        image=SCENES[args.nivel%10](); image.save(args.salida); return
    sheet=Image.new("RGB",(W*2,H*5))
    for i,fn in enumerate(SCENES): sheet.paste(fn(),((i%2)*W,(i//2)*H))
    Path(args.salida).parent.mkdir(parents=True,exist_ok=True); sheet.save(args.salida)
    print(f"{args.salida}: {sheet.width}x{sheet.height}, 10 escenas")
if __name__=="__main__": main()
