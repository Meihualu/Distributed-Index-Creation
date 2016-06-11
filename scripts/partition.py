#! /usr/bin/env python

import Tkinter as tk
from numpy import sign
from random import randint
from time import sleep, time
from threading import Thread


_HEIGHT = 750
_WIDTH = 1600
_PIX_SIZE = 10
_NUM_BY_HEIGHT = (_HEIGHT/2-1)/_PIX_SIZE
_NUM_BY_WEIDTH = 3*_HEIGHT/4/_PIX_SIZE
_UNIT_CAPACITY = 3
_RADIX = 3
_NUM_OF_TERMS = 14

block_cnt = [[0]*4 for t_idx in range(_NUM_OF_TERMS)]
canvases = []
colors = ['red','orange','yellow','green', 'blue', 'purple', 'pink']


def get_capacity(part_no):
    return _UNIT_CAPACITY*(_RADIX**part_no)

def set_block(canvas, n, color):
    canvas.create_rectangle(4+x*_PIX_SIZE, y*_PIX_SIZE, 
            4+(x+1)*_PIX_SIZE, (y+2)*_PIX_SIZE, fill=color)


def set_frames(canvas, color, part_no):
    capacity = get_capacity(part_no)
    for n in range(_NUM_OF_TERMS):
        canvas.create_rectangle(4*_PIX_SIZE, (4+2*n)*_PIX_SIZE,
            (capacity+4)*_PIX_SIZE, (6+2*n)*_PIX_SIZE, outline=color)


def clear_slot(term_idx, part_no):
    block_cnt[term_idx][part_no] = 0
    capacity = get_capacity(part_no)
    canvases[part_no].create_rectangle(4*_PIX_SIZE, (4+2*term_idx)*_PIX_SIZE,
            (capacity+4)*_PIX_SIZE, (6+2*term_idx)*_PIX_SIZE, 
            fill='black', outline='white')


def add_block(term_idx, part_no):
    n = block_cnt[term_idx][part_no]
    canvases[part_no].create_rectangle((4+n)*_PIX_SIZE, (4+2*term_idx)*_PIX_SIZE,
            (5+n)*_PIX_SIZE, (6+2*term_idx)*_PIX_SIZE, 
            fill=colors[term_idx%len(colors)], outline='white')
    sleep(.01)
    block_cnt[term_idx][part_no] += 1


def add_term(term_idx):
    num = 0
    for part_no in range(4):
        if block_cnt[term_idx][part_no]+num < get_capacity(part_no):
            #print term_idx, part_no
            for i in range(part_no):
                clear_slot(term_idx, i)
            for i in range(num):
                add_block(term_idx, part_no)
            add_block(term_idx, 0)
            return True
        else:
            num += block_cnt[term_idx][part_no]
    return False


class Display(Thread):

    def __init__(self):
        Thread.__init__(self)
        self.daemon = True

    def run(self):
        while True:
            start = time()
            for term_idx in range(_NUM_OF_TERMS):
                cnt_texts[term_idx].delete("1.0", tk.END)
                cnt_texts[term_idx].insert("1.0", "0")
                for part_no in range(4):
                    clear_slot(term_idx, part_no)
            term_idx = randint(0, _NUM_OF_TERMS-1)
            while add_term(term_idx):
                cnt_texts[term_idx].delete("1.0", tk.END)
                cnt_texts[term_idx].insert("1.0", sum(block_cnt[term_idx]))
                term_idx = randint(0, _NUM_OF_TERMS-1)
            print time()-start
            

if __name__=='__main__':
    win = tk.Tk()
    win.title("Geometric Partitioning")
    win.geometry(str(_WIDTH)+"x"+str(_HEIGHT))
    grey_bg = tk.Canvas(win, width=_WIDTH, height=_HEIGHT,
            borderwidth=0,
            highlightthickness=0,
            background='#c3c4d3')
    grey_bg.pack()
    tmp_w = (_WIDTH-400)/(1+_RADIX)
    canvases.append(tk.Canvas(grey_bg, width=tmp_w, height=_HEIGHT/2-1, bg='black'))
    canvases.append(tk.Canvas(grey_bg, width=tmp_w, height=_HEIGHT/2-1, bg='black'))
    canvases.append(tk.Canvas(grey_bg, width=_RADIX*tmp_w, height=_HEIGHT/2-1, bg='black'))
    canvases.append(tk.Canvas(grey_bg, width=_RADIX*tmp_w, height=_HEIGHT/2-1, bg='black'))
    canvases[0].place(x=0, y=0)
    canvases[1].place(x=0, y=_HEIGHT/2)
    canvases[2].place(x=tmp_w, y=0)
    canvases[3].place(x=tmp_w, y=_HEIGHT/2)
    for i in range(4):
        set_frames(canvases[i], 'white', i)
    global cnt_texts
    cnt_texts = [tk.Text(grey_bg, font=('Helvetica', 15), borderwidth=0,
        highlightthickness=0, width=5, height=1, fg=colors[i%len(colors)], bg=grey_bg.cget('bg'))
        for i in range(_NUM_OF_TERMS)]
    for i in range(len(cnt_texts)):
        cnt_texts[i].place(x=_WIDTH-380, y=40*i+10)
    Display().start()
    grey_bg.mainloop()

