//
//  IDCardView.m
//  EYE_ImageCollect
//
//  Created by LinYan on 01/12/2017.
//

#import "LHSIDCardScaningView.h"

// iPhone5/5c/5s/SE 4英寸 屏幕宽高：320*568点 屏幕模式：2x 分辨率：1136*640像素
#define iPhone5or5cor5sorSE ([UIScreen mainScreen].bounds.size.height == 568.0)

// iPhone6/6s/7 4.7英寸 屏幕宽高：375*667点 屏幕模式：2x 分辨率：1334*750像素
#define iPhone6or6sor7 ([UIScreen mainScreen].bounds.size.height == 667.0)

// iPhone6 Plus/6s Plus/7 Plus 5.5英寸 屏幕宽高：414*736点 屏幕模式：3x 分辨率：1920*1080像素
#define iPhone6Plusor6sPlusor7Plus ([UIScreen mainScreen].bounds.size.height == 736.0)

@interface LHSIDCardScaningView () {
    CAShapeLayer *_IDCardScanningWindowLayer;
    NSTimer *_timer;
}

@end

@implementation LHSIDCardScaningView

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
    
    CGFloat width = iPhone5or5cor5sorSE? 240: (iPhone6or6sor7? 270: 300);
    _IDCardScanningWindowLayer.bounds = (CGRect){CGPointZero, {width * 1.174,width*0.9}};
    _IDCardScanningWindowLayer.cornerRadius = 15;
    _IDCardScanningWindowLayer.borderColor = [UIColor whiteColor].CGColor;
    _IDCardScanningWindowLayer.borderWidth = 1.5;
    [self.layer addSublayer:_IDCardScanningWindowLayer];
    
    // 最里层镂空
    UIBezierPath *transparentRoundedRectPath = [UIBezierPath bezierPathWithRoundedRect:_IDCardScanningWindowLayer.frame cornerRadius:_IDCardScanningWindowLayer.cornerRadius];
    
    // 最外层背景
    UIBezierPath *path = [UIBezierPath bezierPathWithRect:self.frame];
    [path appendPath:transparentRoundedRectPath];
    [path setUsesEvenOddFillRule:YES];
    
    
    // 提示标签
    CGPoint center = self.center;
    center.y = CGRectGetMinY(_IDCardScanningWindowLayer.frame)-20;
    [self addTipLabelWithText:@"请将证件编号和身份证对准下面白色框框区域，" center:center];
}

#pragma mark - 添加提示标签
-(void )addTipLabelWithText:(NSString *)text center:(CGPoint)center {
    UILabel *tipLabel = [[UILabel alloc] init];
    
    tipLabel.text = text;
    tipLabel.textColor = [UIColor whiteColor];
    tipLabel.textAlignment = NSTextAlignmentLeft;
    
    // tipLabel.transform = CGAffineTransformMakeRotation(M_PI * 0.5);
    [tipLabel sizeToFit];
    
    tipLabel.center = center;
    
    [self addSubview:tipLabel];
    
    
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
    CGContextSetRGBStrokeColor(context,0.3,0.8,0.3,0.8);
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

@end
