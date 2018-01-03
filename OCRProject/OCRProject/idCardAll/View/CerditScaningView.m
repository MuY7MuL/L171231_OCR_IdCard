//
//  LHSIDCardScaningView.m
//  身份证识别
//
//

#import "CerditScaningView.h"

// iPhone5/5c/5s/SE 4英寸 屏幕宽高：320*568点 屏幕模式：2x 分辨率：1136*640像素
#define iPhone5or5cor5sorSE ([UIScreen mainScreen].bounds.size.height == 568.0)

// iPhone6/6s/7 4.7英寸 屏幕宽高：375*667点 屏幕模式：2x 分辨率：1334*750像素
#define iPhone6or6sor7 ([UIScreen mainScreen].bounds.size.height == 667.0)

// iPhone6 Plus/6s Plus/7 Plus 5.5英寸 屏幕宽高：414*736点 屏幕模式：3x 分辨率：1920*1080像素
#define iPhone6Plusor6sPlusor7Plus ([UIScreen mainScreen].bounds.size.height == 736.0)

@interface CerditScaningView () {
    CAShapeLayer *_IDCardScanningWindowLayer;
    NSTimer *_timer;
}

@end

@implementation CerditScaningView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self.backgroundColor = [UIColor clearColor];
        
        // 添加扫描窗口
        [self addScaningWindow];
        
        // 添加定时器
        [self addTimer];
    }
    
    return self;
}

#pragma mark - 添加扫描窗口
-(void)addScaningWindow {
    // 中间包裹线
    _IDCardScanningWindowLayer = [CAShapeLayer layer];
    _IDCardScanningWindowLayer.position = self.layer.position;
    CGFloat width = [UIScreen mainScreen].bounds.size.width;
    CGFloat height = [UIScreen mainScreen].bounds.size.height;
    _IDCardScanningWindowLayer.bounds = (CGRect){CGPointZero, {width, height}};
    _IDCardScanningWindowLayer.cornerRadius = 15;
    _IDCardScanningWindowLayer.borderColor = [UIColor whiteColor].CGColor;
    _IDCardScanningWindowLayer.borderWidth = 1.5;
    [self.layer addSublayer:_IDCardScanningWindowLayer];
    
//    // 最里层镂空
//    UIBezierPath *transparentRoundedRectPath = [UIBezierPath bezierPathWithRoundedRect:_IDCardScanningWindowLayer.frame cornerRadius:_IDCardScanningWindowLayer.cornerRadius];
//
//    // 最外层背景
//    UIBezierPath *path = [UIBezierPath bezierPathWithRect:self.frame];
//    [path appendPath:transparentRoundedRectPath];
//    [path setUsesEvenOddFillRule:YES];
//
//    CAShapeLayer *fillLayer = [CAShapeLayer layer];
//    fillLayer.path = path.CGPath;
//    fillLayer.fillRule = kCAFillRuleEvenOdd;
//    fillLayer.fillColor = [UIColor blackColor].CGColor;
//    fillLayer.opacity = 0.6;
//
//    [self.layer addSublayer:fillLayer];
    
}

#pragma mark - 添加定时器
-(void)addTimer {
    _timer = [NSTimer scheduledTimerWithTimeInterval:0.02 target:self selector:@selector(timerFire:) userInfo:nil repeats:YES];
    [_timer fire];
}

-(void)timerFire:(id)notice {
    [self setNeedsDisplay];
}

-(void)dealloc {
    [_timer invalidate];
}

- (void)drawRect:(CGRect)rect {
    rect = _IDCardScanningWindowLayer.frame;
    
    // 水平扫描线
    CGContextRef context = UIGraphicsGetCurrentContext();
    // 竖直扫描线
         static CGFloat moveY = 0;
         static CGFloat distanceY = 0;
         CGPoint p3, p4;// p3, p4连成竖直扫描线
    
         moveY += distanceY;
         if (moveY >= CGRectGetHeight(rect) - 2) {
         distanceY = -2;
         } else if (moveY <= 2) {
         distanceY = 2;
         }
         p3 = CGPointMake(rect.origin.x, rect.origin.y + moveY);
         p4 = CGPointMake(rect.origin.x + rect.size.width, rect.origin.y + moveY);
    
         CGContextMoveToPoint(context,p3.x, p3.y);
         CGContextAddLineToPoint(context, p4.x, p4.y);
    
    
    CGContextStrokePath(context);
}

// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
//- (void)drawRect:(CGRect)rect {
//    // Drawing code
//    [[UIColor colorWithWhite:0 alpha:0.7] setFill];
//    // 半透明区域
//    UIRectFill(rect);
//
//    // 透明区域
//    CGRect holeRection = self.layer.sublayers[0].frame;
//    /** union: 并集
//     CGRect CGRectUnion(CGRect r1, CGRect r2)
//     返回并集部分rect
//     */
//
//    /** Intersection: 交集
//     CGRect CGRectIntersection(CGRect r1, CGRect r2)
//     返回交集部分rect
//     */
//    CGRect holeiInterSection = CGRectIntersection(holeRection, rect);
//    [[UIColor clearColor] setFill];
//
//    //CGContextClearRect(ctx, <#CGRect rect#>)
//    //绘制
//    //CGContextDrawPath(ctx, kCGPathFillStroke);
//    UIRectFill(holeiInterSection);
//}

@end


