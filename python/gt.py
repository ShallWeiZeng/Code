import requests
import json
import time
import datetime
import re
import execjs
import urllib.request
import random
import sys
import UserAgent
import math
from PIL import Image
from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
import ProxyService

'''70%正确率'''

jsScript = '''
function getUserResponse(a,b){
        for (var c = b.slice(32), d = [], e = 0; e < c.length; e++) {
            var f = c.charCodeAt(e);
            d[e] = f > 57 ? f - 87 : f - 48
        }
        c = 36 * d[0] + d[1];
        var g = Math.round(a) + c;
        b = b.slice(0, 32);
        var h, i = [[], [], [], [], []], j = {}, k = 0;
        e = 0;
        for (var l = b.length; e < l; e++)
            h = b.charAt(e),
            j[h] || (j[h] = 1,
            i[k].push(h),
            k++,
            k = 5 == k ? 0 : k);
        for (var m, n = g, o = 4, p = "", q = [1, 2, 5, 10, 50]; n > 0; )
            n - q[o] >= 0 ? (m = parseInt(Math.random() * i[o].length, 10),
            p += i[o][m],
            n -= q[o]) : (i.splice(o, 1),
            q.splice(o, 1),
            o -= 1);
        return p
}
function getEncoding(a){
    return encodeURIComponent(a)
}
'''
a = execjs.compile(jsScript)


def getHeader():
    headers = {
        'Host': 'www.gsxt.gov.cn',
        'Referer': 'http://www.gsxt.gov.cn/index.html',
        'Connection': 'keep-alive',
        'Accept': 'application/json, text/javascript, */*; q=0.01',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0'
    }
    return headers


def gee_c(a):
    e = []
    f = 0
    g = []
    h = 0
    for h in range(len(a) - 1):
        b = int(round(a[h + 1][0] - a[h][0]))
        c = int(round(a[h + 1][1] - a[h][1]))
        d = int(round(a[h + 1][2] - a[h][2]))
        g.append([b, c, d])
        if b == 0 and c == 0 and d == 0:
            pass
        elif b == 0 and c == 0:
            f += d
        else:
            e.append([b, c, d + f])
            f = 0
    if f != 0:
        e.append([b, c, f])
    return e


def gee_d(a):
    b = "()*,-./0123456789:?@ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqr"
    c = len(b)
    d = ""
    e = abs(a)
    f = int(e / c)
    if f >= c:
        f = c - 1
    if f:
        d = b[f]
    e %= c
    g = ""
    if a < 0:
        g += "!"
    if d:
        g += "$"
    return g + d + b[e]


def gee_e(a):
    b = [[1, 0], [2, 0], [1, -1], [1, 1], [0, 1], [0, -1], [3, 0], [2, -1], [2, 1]]
    c = "stuvwxyz~"
    for d in range(len(b)):
        if a[0] == b[d][0] and a[1] == b[d][1]:
            return c[d]
    return 0


def gee_f(a):
    g = []
    h = []
    i = []
    for j in range(len(a)):
        b = gee_e(a[j])
        if b:
            h.append(b)
        else:
            g.append(gee_d(a[j][0]))
            h.append(gee_d(a[j][1]))
        i.append(gee_d(a[j][2]))
    return "".join(g) + "!!" + "".join(h) + "!!" + "".join(i)


def getA():
    c = [39, 38, 48, 49, 41, 40, 46, 47, 35, 34, 50, 51, 33, 32, 28, 29, 27, 26, 36, 37, 31, 30, 44, 45, 43, 42, 12, 13,
         23, 22, 14, 15, 21, 20, 8, 9, 25, 24, 6, 7, 3, 2, 0, 1, 11, 10, 4, 5, 19, 18, 16, 17]

    return c


def user_response(a, b):
    c = b[32:]

    d = []
    for i in range(len(c)):
        f = ord(c[i])
        d.append(f - 87 if f > 57 else f - 48)
    c = 36 * d[0] + d[1]
    g = round(a) + c
    b = b[0:32]
    i = [[], [], [], [], []]
    j = {}
    k = 0
    for e in range(len(b)):
        h = b[e]
        if h not in j:
            j[h] = 1
            i[k].append(h)
            k = k + 1
            if k == 5: k = 0
    n = g
    o = 4
    p = ""
    q = [1, 2, 5, 10, 50]
    while n > 0:
        if n - q[o] >= 0:
            m = int(random.random() * len(i[o]))
            p += str(i[o][m])

            n -= q[o]
        else:
            i = i[:o] + i[o + 1:]
            q = q[:o] + q[o + 1:]
            o -= 1
    return p


def getImage(imgPath, filePath):
    img = Image.open(imgPath)
    c = getA()
    row = 2
    column = 26
    width = 10
    height = img.size[1] / row
    newImage = Image.new('RGB', (260, img.size[1]), (255, 255, 255))
    for i in range(row):
        for j in range(column):
            offsetX = c[i * column + j] % 26 * 12
            if (c[i * column + j] > 25):
                offsetY = int(height)
            else:
                offsetY = 0
            leftUpperX = int(j * width)
            leftUpperY = int(i * height)
            temp = Image.new('RGB', (int(width), int(height)), (255, 255, 255))
            box = (int(offsetX), int(offsetY), int(offsetX + width), int(offsetY + height))
            region = img.crop(box)
            temp.paste(region, (0, 0))
            newImage.paste(temp, (leftUpperX, leftUpperY))

    newImage.save(filePath + ".jpg")


'''
判断每个像素是否相似
'''
def isSimilar(col1, col2):
    # 设置相似的绝对差max
    max = 80
    diffR = abs(col1[0] - col2[0])
    diffG = abs(col1[1] - col2[1])
    diffB = abs(col1[2] - col2[2])

    return diffR < max and diffG < max and diffB < max

'''
判断图片的每一列是否相似
'''
def isColumnSimilar(imgbg, imgfull, height, columnindex):
    for i in range(height):
        if isSimilar(imgbg.getpixel((columnindex, i)), imgfull.getpixel((columnindex, i))) is False:
            return False
    return True

'''
计算移动距离
'''
def getDis(imgbg, imgfull, width, height):
    dis = -1
    for i in range(width):
        if isColumnSimilar(imgbg, imgfull, height, i) is False:
            dis = i
            break

    if dis == -1:
        print("Something wrong")
    return dis

'''
获取距离的另一种写法
'''
def get_offset():
    """得到偏移量, 按照列进行比较, 单位为像素点"""
    bg_im = Image.open('D:\\bgpath.jpg')
    fullbg_im = Image.open('D:\\fullbgpath.jpg')
    width = fullbg_im.size[0]
    height = fullbg_im.size[1]
    for w in range(width):
        for h in range(height):
            # 如果为真则返回宽度
            if diff(bg_im, fullbg_im, w, h):
                return w
    return False

def diff(bg_im, fullbg_im, w, h):
    bg_num = bg_im.getpixel((w, h))
    fullbg_num = fullbg_im.getpixel((w, h))
    # 得到一个元祖
    tmp = 0
    for i in range(3):
        if abs(bg_num[i] - fullbg_num[i]) > 50:
            tmp += 1
    if tmp == 3:
        return True
    return False

'''
得到userresponse
'''
def getUserresponse(movdis, challenge):
    c = challenge[32:]
    d = []
    for e in range(len(c)):
        tem = ord(c[e])
        if tem > 57:
            tem -= 87
        else:
            tem -= 48
        d.append(tem)
    c = 36 * int(d[0]) + int(d[1])
    g = movdis + c
    b = challenge[0:32]
    i = [[], [], [], [], []]
    j = {}
    k = 0
    l = len(b)
    for e in range(l):
        h = b[e]
        if (not j.get(h)):
            j[h] = 1
            i[k].append(h)
            k += 1
            if (k is 5):
                k = 0
    n = g
    o = 4
    p = ""
    q = [1, 2, 5, 10, 50];
    while int(n) > 0:
        if n - q[o] >= 0:
            m = int(random.random() * len(i[o]))
            p += str(i[o][m])
            n -= q[o]
        else:
            i.remove(i[o])
            q.remove(q[o])
            o -= 1
    return p


'''
模拟轨迹
'''

def getTrace5(dis):
    # 规划路径，分为三种
    length = dis
    res = []
    pre = 20
    lastX = 0
    lastY = 0
    # 第一个值为点击的位置的相反数, 方块大小是44*44
    first_list = [-random.randint(1, 43), -random.randint(1, 43), 0]
    # 第二个值永远为[0, 0, 0]
    second_param = [0, 0, 0]
    res.append(first_list)
    res.append(second_param)
    # roundTime = random.randint(1, 3)
    # leftOffset = random.randint(10, 15)
    if length < 60:
        total = 0
        total2 = 0
        for i in range(length):
            total += 1
            total2 += 1
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
            time.sleep(sleep * 0.001)
            lastY = lastY + random.randint(-1, 1)
            res.append([1 + lastX, lastY, t])
            lastX = 1 + lastX
            if total2 >= length - 15:
                for index in range(3):
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    res.append([lastX + 9999, lastY + 9999, t])
                    time.sleep(sleep * 0.001)
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    time.sleep(sleep * 0.001)
                    res.append([lastX, lastY, t])
                total2 = 0
    elif length >= 60 and length < 120:
        total = 0
        total2 = 0
        for i in range(int(length / 2 + 1)):
            total += 2
            total2 += 2
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
            lastY = lastY + random.randint(-1, 1)
            if total <= length:
                res.append([2 + lastX, lastY, t])
                lastX += 2
            else:
                res.append([length, lastY, t])
                lastX = res[len(res) - 1][0]
            if total2 >= length - 15:
                for index in range(3):
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    res.append([lastX + 9999, lastY + 9999, t])
                    time.sleep(sleep * 0.001)
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    time.sleep(sleep * 0.001)
                    res.append([lastX, lastY, t])
                total2 = 0
            time.sleep(sleep * 0.001)
    else:
        total = 0
        total2 = 0
        for i in range(int(length / 3 + 1)):
            total += 3
            total2 += 3
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
            lastY = lastY + random.randint(-1, 1)
            if total <= length:
                res.append([3 + lastX, lastY, t])
                lastX += 3
            else:
                res.append([length, lastY, t])
                lastX = res[len(res) - 1][0]
            time.sleep(sleep * 0.001)
            if total2 >= length - 15:
                for index in range(3):
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    res.append([lastX + 9999, lastY + 9999, t])
                    time.sleep(sleep * 0.001)
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    time.sleep(sleep * 0.001)
                    res.append([lastX, lastY, t])
                total2 = 0
    return res

#不设阶段，每次爬取随机1~3的整数
def getTrace6(dis):
    length = dis
    res = []
    pre = random.randint(20, 30)
    lastX = 0
    lastY = 0
    first_list = [-random.randint(1, 43), -random.randint(1, 43), 0]
    second_param = [0, 0, 0]
    res.append(first_list)
    res.append(second_param)
    total = 0
    total2 = 0
    t = 0
    leftoffset = random.randint(5, 15)
    if(length + leftoffset) > 260:
        moveoffset = 260
    else:
        moveoffset = (leftoffset + length)
    while total < moveoffset:
        offset = random.randint(1, 6)
        total += offset
        total2 += offset
        sleep = random.randint(10, 90)
        t = pre + sleep
        pre = t
        time.sleep(sleep * 0.001)
        lastX = lastX + offset
        lastY = lastY + random.randint(-1, 1)
        res.append([lastX, lastY, t])
    current = res[len(res) - 1][0]

    while current > length:
        offset = random.randint(1, 2)
        current -= offset
        sleep = random.randint(10, 90)
        t = pre + sleep
        pre = t
        time.sleep(sleep * 0.001)
        lastX -= offset
        lastY += random.randint(-1, 1)
        res.append([lastX, lastY, t])
    sleep = random.randint(100, 150)
    time.sleep(sleep * 0.001)
    t = pre + sleep
    res.append([length, lastY, t])
    return res

'''首次尝试加速度'''
def getTrace7(dis):
    res = []
    current = 0
    mid = dis * 4 / 5
    t = 0.2
    v = 0
    first_list = [-random.randint(1, 43), -random.randint(1, 43), 0]
    second_param = [0, 0, 0]
    res.append(first_list)
    res.append(second_param)
    pre = random.randint(50, 150)
    while current < dis:
        if current < mid:
            a = 2
        else:
            a = -3
        v0 = v
        v = v0 + a * t
        move = v0 * t + 1/2 * a * t * t
        current += move
        sleep = random.randint(10, 90)
        time.sleep(sleep * 0.001)
        tt = pre + sleep
        pre = tt
        res.append([round(current), 0, tt])
    sleep = random.randint(10, 150)
    time.sleep(sleep * 0.001)
    tt = pre + sleep
    res.append([dis, 0, tt])
    return res

def getTrace8(dis):
    setpointX = dis
    offset = dis
    '''
    切记不能移动小数个像素位置
    '''
    kp = 3.0
    ki = 0.0001
    kd = 80.0

    x = 0
    vx = 0
    prevErrorX = 0
    integralX = 0
    derivativeX = 0
    res = []
    # 第一个值为点击的位置的相反数, 方块大小是44*44
    first_list = [-random.randint(1, 43), -random.randint(1, 43), 0]
    # 第二个值永远为[0, 0, 0]
    second_param = [0, 0, 0]
    res.append(first_list)
    res.append(second_param)
    lastY = 0
    pre = random.randint(10, 20)
    while 1:
        if x >= setpointX:
            break

        errorX = setpointX - x
        # print('xxxxx - ', x)
        integralX += errorX
        derivativeX = errorX - prevErrorX
        prevErrorX = errorX
        if offset < 100:
            K = 0.007
        elif offset < 180:
            K = 0.006
        else:
            K = 0.005
        ax = K * (kp * errorX + ki * integralX + kd * derivativeX)
        vx += ax

        if x + vx > setpointX:
            vx = setpointX - x
        vx = int(vx)
        if vx < 1:
            vx = random.randint(1, 3)
        # yield vx
        # res.append(vx)
        # print('vvvvv - ', vx)
        x += vx
        Y = lastY + random.randint(-1, 1)
        sleep = random.randint(4, 70)
        t = pre + sleep
        pre = t
        res.append([x, Y, t])
        time.sleep(sleep * 0.001)
    return res

def getTrace9(dis):
    # 规划路径，分为三种
    length = dis
    res = []
    pre = 20
    lastX = 0
    lastY = 0
    # 第一个值为点击的位置的相反数, 方块大小是44*44
    first_list = [-random.randint(1, 43), -random.randint(1, 43), 0]
    # 第二个值永远为[0, 0, 0]
    second_param = [0, 0, 0]
    res.append(first_list)
    res.append(second_param)
    # roundTime = random.randint(1, 3)
    # leftOffset = random.randint(10, 15)
    if length < 60:
        total = 0
        total2 = 0
        for i in range(length):
            total += 1
            total2 += 1
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
            time.sleep(sleep * 0.001)
            lastY = lastY + random.randint(-1, 1)
            res.append([1 + lastX, lastY, t])
            lastX = 1 + lastX
            if total2 >= length - 15:
                for index in range(3):
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    res.append([lastX + 9999, lastY + 9999, t])
                    time.sleep(sleep * 0.001)
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    time.sleep(sleep * 0.001)
                    res.append([lastX, lastY, t])
                total2 = 0
    elif length >= 60 and length < 120:
        total = 0
        total2 = 0
        for i in range(int(length / 2 + 1)):
            total += 2
            total2 += 2
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
            lastY = lastY + random.randint(-1, 1)
            if total <= length:
                res.append([2 + lastX, lastY, t])
                lastX += 2
            else:
                res.append([length, lastY, t])
                lastX = res[len(res) - 1][0]
            if total2 >= length - 15:
                for index in range(3):
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    res.append([lastX + 9999, lastX + 9999, t])
                    time.sleep(sleep * 0.001)
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    time.sleep(sleep * 0.001)
                    res.append([lastX, lastY, t])
                total2 = 0
            time.sleep(sleep * 0.001)
    else:
        total = 0
        total2 = 0
        for i in range(int(length / 3 + 1)):
            total += 3
            total2 += 3
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
            lastY = lastY + random.randint(-1, 1)
            if total <= length:
                res.append([3 + lastX, lastY, t])
                lastX += 3
            else:
                res.append([length, lastY, t])
                lastX = res[len(res) - 1][0]
            time.sleep(sleep * 0.001)
            if total2 >= length - 15:
                for index in range(3):
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    res.append([lastX + 9999, lastY + 9999, t])
                    time.sleep(sleep * 0.001)
                    sleep = random.randint(2, 3)
                    t = pre + sleep
                    pre = t
                    time.sleep(sleep * 0.001)
                    res.append([lastX, lastY, t])
                total2 = 0
    return res

def g(t, t0, t1):
     return math.tanh(1.5 * (t-t0)/(t1 - t0))
def getTrace10(dis):
    t1 = random.randint(1000, 1500)
    t0 = random.randint(200, 500)
    gt1 = math.tanh(1.5)
    constant = dis / gt1
    res = []
    pre = t0
    # 第一个值为点击的位置的相反数, 方块大小是44*44
    first_list = [-random.randint(1, 43), -random.randint(1, 43), 0]
    # 第二个值永远为[0, 0, 0]
    second_param = [0, 0, 0]
    res.append(first_list)
    res.append(second_param)
    ly = 0
    flag = True
    adjustTime = random.randint(2, 5)
    while True:
        sleep = random.randint(4, 70)
        t = pre + sleep
        if flag is False:
            break
        if t > t1:
            flag = False
        pre = t
        x = int(constant * g(t, t0, t1))
        y = ly + random.randint(-1, 1)
        ly = y
        res.append([x, y, t])

    prex = res[len(res)-1][0]
    diff  = prex - dis
    first = True
    x = prex
    for i in range(int(adjustTime/2)):
        if diff > 0:
            dx = random.randint(2 * (-diff),0)
        else:
            dx = random.randint(0, -2 * diff)
        diff = dx
        x = x + dx
        y = ly + random.randint(-1, 1)
        ly = y
        if first is True:
            sleep = random.randint(100, 200)
            t = pre + sleep
            pre = t
            first  = False
        else:
            sleep = random.randint(4, 70)
            t = pre + sleep
            pre = t
        res.append([x, y, t])



    sleeptime = t + random.randint(4, 70)
    res.append([dis, ly, sleeptime])
    time.sleep(sleeptime * 0.001)
    return res




def mergeCookie(ck):
    for i in ck:
        try:
            cookie[i] = ck[i]
        except:
            continue
def getCookie():
    res = ""
    for i in cookie:
        if cookie[i] is None:
            res = res + str(i) + "=" + ";"
        else:
            res = res + str(i) + "=" + str(cookie[i]) + ";"
    res = res[0: -1]
    return res
times = 0
success = 0
ipSets = []
dupCount = 0
while (True):
    # print(getTrace10(125))
    # exit(0)
    try:
        session = requests.Session()
        cookie = {}
        userAgent = UserAgent.getPCUserAgent()
        p = getProxy()
        p = json.loads(p)
        print(p[0])
        if p[0] in ipSets:
            dupCount +=1
            print("duplicated IP " + str(p[0]))
            print("dupCount : " + str(dupCount))
        else:
            ipSets.append(p[0])
        proxy = {"http": + str(p[0]), "https": "" + str(p[0])}


        header = getHeader()
        header['User-Agent'] = userAgent
        header['Cookie'] = getCookie()
        timeStamp = int(round(time.time() * 1000))



        '''
        得到gt和challenge
        '''
        header['Cookie'] = getCookie()
        doc = session.get("http://www.gsxt.gov.cn/SearchItemCaptcha?v=" + str(timeStamp), headers=header,
                          timeout=30, proxies=proxy)
        mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))
        first = doc.json()
        gt = first['gt']
        challenge = first['challenge']
        '''
        第二个包
        '''
        ran = random.randrange(0, 10000)
        timeStamp = int(round(time.time() * 1000))
        twoHeader = {
            'Host': 'api.geetest.com',
            'User-Agent': userAgent,
            'Accept': '*/*',
            'Accept-Language': 'zh-CN,zh;q=0.8',
            'Accept-Encoding': 'gzip, deflate',
            'Referer': 'http://www.gsxt.gov.cn/index.html'
        }
        doc = session.get("http://api.geetest.com/gettype.php?gt=" + str(gt) + "&callback=geetest_" + str(timeStamp),
                          headers=twoHeader, timeout=30, proxies=proxy)
        mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))

        '''
        获得第三个包
        '''
        header['Host'] = 'api.geetest.com'
        header['Cookie'] = "GeeTestUser=" + cookie["GeeTestUser"]
        try:
            header['Cookie'] = ";aliyungf_tc" + cookie['aliyungf_tc']
        except:
            pass
        timeStamp = int(datetime.datetime.now().timestamp() * 1000)
        print(timeStamp)
        param = "gt=" + gt + "&challenge=" + challenge + "&product=popup" + \
                "&offline=false&protocol=&path=/static/js/geetest.5.10.10.js&type=slide&callback=geetest_" \
                + str(timeStamp)
        doc = session.get("http://api.geetest.com/get.php?" + param, headers=header, timeout=30, proxies=proxy)
        mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))
        doc = doc.text.replace("geetest_" + str(timeStamp), "")
        doc = doc[1:-1]
        secend = json.loads(str(doc))
        bgpath = secend.get('bg')
        fullbgpath = secend.get('fullbg')
        '''
        获得图片,并保存图片到本地
        '''
        header['Host'] = 'static.geetest.com'
        ta = session.get("http://static.geetest.com/" + bgpath, headers=header, proxies=proxy, timeout=30)
        tb = session.get("http://static.geetest.com/" + fullbgpath, headers=header, proxies=proxy, timeout=30)
        urllib.request.urlretrieve("http://static.geetest.com/" + bgpath, 'D:\\bgimage.jpg')
        urllib.request.urlretrieve("http://static.geetest.com/" + fullbgpath, 'D:\\fullimage.jpg')
        bgpath = "D:\\bgimage.jpg"
        fullbgpath = 'D:\\fullimage.jpg'
        '''
        读取图片 ,拼图
        '''
        getImage(bgpath, "D:\\bgpath")
        getImage(fullbgpath, "D:\\fullbgpath")
        '''
        图像拼接获取移动距离 用于生成userresponse
        '''
        imgbg = Image.open("D:\\bgpath.jpg")
        imgfull = Image.open("D:\\fullbgpath.jpg")
        dis = getDis(imgbg, imgfull, 260, imgbg.size[1])
        print("first dis:")
        print(get_offset())
        print("dis:" + str(dis))
        movedis = int(dis - 6)
        # time.sleep(random.randint(5, 10))
        a = execjs.compile(jsScript)
        userresponse = user_response(movedis, secend.get("challenge"))
        '''
        模拟鼠标轨迹
       '''
        moveTrace = getTrace10(movedis)
        print(moveTrace)
        result = gee_f(gee_c(moveTrace))
        result = a.call("getEncoding", result)
        # result = result.encode('utf-8')
        timeStamp = int(round(time.time() * 1000))
        param = "a=" + str(result) + "&gt=" + gt + "&challenge=" + secend.get("challenge") + "&callback=geetest_" + str(
            timeStamp) + "&imgload=" + str(random.randint(50, 150)) + "&passtime=" + str(
            moveTrace[len(moveTrace) - 1][2]) + "&userresponse=" + str(userresponse)
        header['Host'] = 'api.geetest.com'
        header['Cookie'] = "GeeTestUser=" + cookie["GeeTestUser"]

        header['Accept-Encoding'] = "gzip, deflate, sdch"
        header['Accept-Language'] = 'zh-CN,zh;q=0.8'
        header['Accept'] = '*/*'
        validate = session.get("http://api.geetest.com/ajax.php?" + param, headers=header, timeout=30, proxies=proxy)
        mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))
        v = validate.text.replace("geetest_" + str(timeStamp), "")
        v = v[1:-1]

        v = json.loads(v)
        cnt = 0
        # print(v)
        '''尝试重试'''
        while v["message"] == "forbidden" or v["message"] == "fail":
            if cnt == 4:
                break
            header = {
                "Accept": "* / *",
                "Accept - Encoding": "gzip, deflate, sdch",
                "Accept - Language": "zh - CN, zh;q = 0.8",
                "Cache - Control": "no - cache",
                "Host": "api.geetest.com",
                "Referer": "http://www.gsxt.gov.cn/index.html",
                "User-Agent": userAgent
            }
            timeStamp = int(round(time.time() * 1000))
            param = "challenge=" + str(secend.get("challenge")) + "&gt=" + gt + "&callback=geetest_" + str(timeStamp)
            header['Cookie'] = "GeeTestAjaxUser=" + cookie["GeeTestAjaxUser"] + ";GeeTestUser=" + cookie["GeeTestUser"]
            doc = session.get("http://api.geetest.com/refresh.php?" + param, headers=header, timeout=30, proxies=proxy)
            mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))
            doc = doc.text.replace("geetest_" + str(timeStamp), "")
            doc = doc[1:-1]
            doc = json.loads(doc)

            challenge = doc["challenge"]
            bgpath = doc["bg"]
            fullbgpath = doc["fullbg"]
            ta = session.get("http://static.geetest.com/" + bgpath, headers=header, proxies=proxy, timeout=30)
            tb = session.get("http://static.geetest.com/" + fullbgpath, headers=header, proxies=proxy, timeout=30)
            mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))
            urllib.request.urlretrieve("http://static.geetest.com/" + bgpath, 'D:\\bgimage.jpg')
            urllib.request.urlretrieve("http://static.geetest.com/" + fullbgpath, 'D:\\fullimage.jpg')
            bgpath = "D:\\bgimage.jpg"
            fullbgpath = 'D:\\fullimage.jpg'
            getImage(bgpath, "D:\\bgpath")
            getImage(fullbgpath, "D:\\fullbgpath")
            imgbg = Image.open("D:\\bgpath.jpg")
            imgfull = Image.open("D:\\fullbgpath.jpg")
            dis = getDis(imgbg, imgfull, 260, imgbg.size[1])

            movedis = int(dis - 6)
            userresponse = user_response(movedis, challenge)
            moveTrace = getTrace10(movedis)
            result = gee_f(gee_c(moveTrace))
            timeStamp = int(round(time.time() * 1000))
            header['Cookie'] = "GeeTestAjaxUser=" + cookie["GeeTestAjaxUser"] + ";GeeTestUser=" + cookie["GeeTestUser"]
            param = "a=" + str(result) + "&gt=" + gt + "&challenge=" + challenge + "&callback=geetest_" + str(
                timeStamp) + "&imgload=" + str(random.randint(50, 150)) + "&passtime=" + str(
                moveTrace[len(moveTrace) - 1][2]) + "&userresponse=" + str(userresponse)
            validate = session.get("http://api.geetest.com/ajax.php?" + param, headers=header,timeout=30, proxies=proxy)
            mergeCookie(requests.utils.dict_from_cookiejar(session.cookies))
            v = validate.text.replace("geetest_" + str(timeStamp), "")
            v = v[1:-1]
            v = json.loads(v)
            print("try times:" + str(cnt))
            time.sleep(4)
            cnt += 1
        '''重试'''
        if (v["message"] == "success"):
            success += 1
        times += 1
        print(v)
        # time.sleep(4)
        print("success:" + str(success) + " total:" + str(times))
    except Exception as err:
        print(err)
print("success radio:" + str(success))