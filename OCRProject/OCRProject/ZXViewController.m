//
//  ZXViewController.m
//  EYE_ImageCollect
//
//  Created by LinYan on 01/12/2017.
//

#import "ZXViewController.h"
#import <AudioToolbox/AudioToolbox.h>
#import <MediaPlayer/MediaPlayer.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <AVFoundation/AVFoundation.h>
#import <AssetsLibrary/AssetsLibrary.h>

#import "UIAlertController+Extend.h"
#import "IDInfo.h"
#import "excards.h"
#import "unistd.h"
#import "RectManager.h"
#import "InfoTools.h"

#import "IDInfoViewController.h"

typedef void(^PropertyChangeBlock)(AVCaptureDevice *captureDevice);
@interface ZXViewController ()<AVCaptureVideoDataOutputSampleBufferDelegate,AVCaptureMetadataOutputObjectsDelegate,UINavigationControllerDelegate>
{
    CGFloat x;
    CGFloat y;
    CGFloat z;
    BOOL isClick;// 判断按钮是否被点击
}
@property (nonatomic,strong) AVCaptureVideoDataOutput *captureVideoDataOutput;
//负责输入和输出设备之间的数据传递
@property (strong,nonatomic) AVCaptureSession *captureSession;
//负责从AVCaptureDevice获得输入数据
@property (strong,nonatomic) AVCaptureDeviceInput *captureDeviceInput;
//照片输出流
@property (strong,nonatomic) AVCaptureStillImageOutput *captureStillImageOutput;
//相机拍摄预览图层
@property (strong,nonatomic) AVCaptureVideoPreviewLayer *captureVideoPreviewLayer;
// 映射图层
@property (nonatomic,strong)UIView *videoMainView;
// 聚焦光圈
@property (nonatomic,strong)UIImageView *focusCursor;
// 拍照按钮
@property (nonatomic,strong)UIButton *camBtn;
// 转换
@property (nonatomic,strong)UIImageView *imageView;
// 控制拍照按钮
@property (nonatomic,strong)NSTimer *timer;
// 扫描到标记
@property (nonatomic,strong)UILabel *label;
@property (nonatomic,strong)NSMutableArray *labelData;
//底部取消按钮
@property (nonatomic, strong)UIButton *overlayCancelBtn;
//底部拍照按钮
@property (nonatomic, strong)UIButton *overlayTakePhotoBtn;
//底部摄像头返回按钮
@property (nonatomic, strong)UIButton *overlaySwitchDeviceBtn;
//人脸检测框区域
@property (nonatomic,assign) CGRect faceDetectionFrame;
// 元数据（用于人脸识别）
@property (nonatomic,strong) AVCaptureMetadataOutput *metadataOutput;
// 输出格式
@property (nonatomic,strong) NSNumber *outPutSetting;
@end

@implementation ZXViewController

- (NSMutableArray *)labelData
{
    if (_labelData == nil) {
        _labelData = [NSMutableArray array];
    }
    return _labelData;
}

- (UIImageView *)imageView
{
    if (_imageView == nil) {
        _imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, SCR_WIDTH, SCR_HEIGHT)];
        _imageView.backgroundColor = [UIColor clearColor];
    }
    return _imageView;
}
- (UIView *)videoMainView
{
    if (_videoMainView == nil) {
        _videoMainView = [[UIView alloc] init];
        _videoMainView.frame = CGRectMake(0, 0, SCR_WIDTH ,SCR_HEIGHT);
    }
    return _videoMainView;
}

- (UIButton *)camBtn
{
    if (_camBtn == nil) {
        _camBtn  = [[UIButton alloc] initWithFrame:CGRectMake(0, 60, 200, 80)];
        [_camBtn setBackgroundColor:[UIColor clearColor]];
    }
    return _camBtn;
}

#pragma mark metadataOutput
-(AVCaptureMetadataOutput *)metadataOutput {
    if (_metadataOutput == nil) {
        _metadataOutput = [[AVCaptureMetadataOutput alloc]init];
        [_metadataOutput setMetadataObjectsDelegate:self queue:dispatch_get_current_queue()];
    }
    return _metadataOutput;
}

#pragma mark outPutSetting
-(NSNumber *)outPutSetting {
    if (_outPutSetting == nil) {
        _outPutSetting = @(kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange);
    }
    return _outPutSetting;
}

-(void) addButtonView{
    CGFloat width = self.videoMainView.frame.size.width;
    CGFloat height = self.videoMainView.frame.size.height;
    CGFloat imageHeight = width * 1000 / 750;
    CGFloat buttonWidth = 50;
    CGFloat buttonHeight = 50;
    CGFloat buttonY = imageHeight + (height - imageHeight - buttonHeight) / 2+30;
    CGFloat buttonX1 = (width / 3 - buttonWidth) / 2;
    CGFloat buttonX2 = (width / 3 - buttonWidth) / 2 + width / 3;
    CGFloat buttonX3 = (width / 3 - buttonWidth) / 2 + width / 3 * 2;
    
    self.overlayCancelBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.overlayCancelBtn setTitle:@"取消" forState:UIControlStateNormal];
    self.overlayCancelBtn.frame = CGRectMake(buttonX1, buttonY, buttonWidth, buttonHeight);
    //    [self.overlayCancelBtn addTarget:self action:@selector(cancelPhoto:) forControlEvents:UIControlEventTouchUpInside];
    
    self.overlayTakePhotoBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.overlayTakePhotoBtn setImage:[UIImage imageNamed:@"camera_take"] forState:UIControlStateNormal];
    [self.overlayTakePhotoBtn setImage:[UIImage imageNamed:@"camera_take_press"] forState:UIControlStateHighlighted];
    self.overlayTakePhotoBtn.frame = CGRectMake(buttonX2, buttonY, buttonWidth, buttonHeight);
    [self.overlayTakePhotoBtn addTarget:self action:@selector(takeButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    self.overlayTakePhotoBtn.adjustsImageWhenDisabled = NO;
    
    self.overlaySwitchDeviceBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.overlaySwitchDeviceBtn setImage:[UIImage imageNamed:@"camera_change"] forState:UIControlStateNormal];
    self.overlaySwitchDeviceBtn.frame = CGRectMake(buttonX3, buttonY, buttonWidth, buttonHeight);
    self.overlaySwitchDeviceBtn.imageEdgeInsets = UIEdgeInsetsMake(8, 8, 8, 8);
    
    
    [self.view addSubview:self.overlayCancelBtn];
    [self.view addSubview:self.overlayTakePhotoBtn];
    [self.view addSubview:self.overlaySwitchDeviceBtn];
    
    UIImageView *uiimage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"icon_hand_back"]];
    [uiimage setFrame:CGRectMake(0, 62, SCR_WIDTH, buttonY-buttonHeight-30)];
    [self.view addSubview:uiimage];
}


- (void)viewDidLoad {
    [super viewDidLoad];
        self.navigationItem.title = @"身份识别";
        self.navigationController.delegate = self;
        [self.view addSubview:self.videoMainView];
        [self.videoMainView addSubview:self.camBtn];
        self.camBtn.hidden = YES;
        self.label.hidden = YES;
        [self addButtonView];
        self.faceDetectionFrame = CGRectMake(0, 0, self.videoMainView.frame.size.width/2, self.videoMainView.frame.size.height/2);
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    // 初始化相机
    [self getCameraSession];
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    if (![self.captureSession isRunning]) {
        dispatch_async(dispatch_get_current_queue(),^{
            [self.captureSession startRunning];
        });
    }
}

-(void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
    [self.captureSession stopRunning];
}

#pragma mark - 初始化相机
- (void)getCameraSession
{
    //初始化会话
    _captureSession=[[AVCaptureSession alloc]init];
    if ([_captureSession canSetSessionPreset:AVCaptureSessionPreset1280x720]) {//设置分辨率
        _captureSession.sessionPreset = AVCaptureSessionPreset1280x720;
    }
    //获得输入设备
    AVCaptureDevice *captureDevice=[self getCameraDeviceWithPosition:AVCaptureDevicePositionBack];//取得前置摄像头
    if (!captureDevice) {
        NSLog(@"取得前置摄像头时出现问题.");
        return;
    }
    
    NSError *error=nil;
    //根据输入设备初始化设备输入对象，用于获得输入数据
    _captureDeviceInput=[[AVCaptureDeviceInput alloc]initWithDevice:captureDevice error:&error];
    if (error) {
        NSLog(@"取得设备输入对象时出错，错误原因：%@",error.localizedDescription);
        return;
    }
    [_captureSession addInput:_captureDeviceInput];
    
    //初始化设备输出对象，用于获得输出数据
    _captureStillImageOutput=[[AVCaptureStillImageOutput alloc]init];
    NSDictionary *outputSettings = @{AVVideoCodecKey:AVVideoCodecJPEG};
    [_captureStillImageOutput setOutputSettings:outputSettings];//输出设置
    
    //将设备输入添加到会话中
    if ([_captureSession canAddInput:_captureDeviceInput]) {
        [_captureSession addInput:_captureDeviceInput];
    }
    
    //将设备输出添加到会话中
    if ([_captureSession canAddOutput:_captureStillImageOutput]) {
        [_captureSession addOutput:_captureStillImageOutput];
    }
    
    if ([_captureSession canAddOutput:self.metadataOutput]) {
        
        [_captureSession addOutput:self.metadataOutput];
        // 输出格式要放在addOutPut之后，否则奔溃
        NSArray *typeList = self.metadataOutput.availableMetadataObjectTypes;
        self.metadataOutput.metadataObjectTypes = @[AVMetadataObjectTypeFace];
    }
    
    //创建视频预览层，用于实时展示摄像头状态
    _captureVideoPreviewLayer=[[AVCaptureVideoPreviewLayer alloc]initWithSession:self.captureSession];
    CALayer *layer=self.videoMainView.layer;
    layer.masksToBounds=YES;
    _captureVideoPreviewLayer.frame=layer.bounds;
    _captureVideoPreviewLayer.videoGravity=AVLayerVideoGravityResizeAspectFill;//填充模式
    
    //将视频预览层添加到界面中
    [layer addSublayer:_captureVideoPreviewLayer];
    
    // 初始化数据流
    [self addVidelDataOutput];
}
/**
 *  AVCaptureVideoDataOutput 获取数据流
 */
- (void)addVidelDataOutput
{
    AVCaptureVideoDataOutput *captureOutput = [[AVCaptureVideoDataOutput alloc] init];
    captureOutput.alwaysDiscardsLateVideoFrames = YES;
    dispatch_queue_t queue;
    queue = dispatch_queue_create("myQueue", DISPATCH_QUEUE_SERIAL);
    [captureOutput setSampleBufferDelegate:self queue:queue];
    NSString *key = (NSString *)kCVPixelBufferPixelFormatTypeKey;
    //NSNumber *value = [NSNumber numberWithUnsignedInt:kCVPixelFormatType_32BGRA];
    NSNumber *value = [NSNumber numberWithUnsignedInt:kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange];
    NSDictionary *settings = @{key:value};
    [captureOutput setVideoSettings:settings];
    [self.captureSession addOutput:captureOutput];
}

#pragma mark 拍照
- (void)takeButtonClick:(UIButton *)sender {
    isClick = 1;
}

#pragma mark - 私有方法

/**
 *  取得指定位置的摄像头
 *
 *  @param position 摄像头位置
 *
 *  @return 摄像头设备
 */
-(AVCaptureDevice *)getCameraDeviceWithPosition:(AVCaptureDevicePosition )position{
    NSArray *cameras= [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    for (AVCaptureDevice *camera in cameras) {
        if ([camera position] == position) {
            return camera;
        }
    }
    return nil;
}

#pragma mark 从输出的元数据中捕捉人脸
// 检测人脸是为了获得“人脸区域”，做“人脸区域”与“身份证人像框”的区域对比，当前者在后者范围内的时候，才能截取到完整的身份证图像
-(void)captureOutput:(AVCaptureOutput *)captureOutput
didOutputMetadataObjects:(NSArray *)metadataObjects
      fromConnection:(AVCaptureConnection *)connection{
    if (metadataObjects.count) {
        AVMetadataMachineReadableCodeObject *metadataObject = metadataObjects.firstObject;
        
        AVMetadataObject *transformedMetadataObject = [self.captureVideoPreviewLayer transformedMetadataObjectForMetadataObject:metadataObject];
        CGRect faceRegion = transformedMetadataObject.bounds;
        
        if (metadataObject.type == AVMetadataObjectTypeFace) {
            if (!self.captureVideoDataOutput.sampleBufferDelegate &&CGRectContainsRect(self.faceDetectionFrame, faceRegion)>=1) {
                [self.captureVideoDataOutput setSampleBufferDelegate:self queue:dispatch_get_current_queue()];
                return;
            }
            if(isClick){
                if(CGRectContainsRect(self.faceDetectionFrame, faceRegion)<1){
                    NSString *title = @"提示";
                    NSString *message = @"照片中的人像不清晰，请重新拍摄";
                    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
                    [self alertControllerWithTitle:title message:message okAction:okAction cancelAction:nil];
                }else{
                    if (!self.captureVideoDataOutput.sampleBufferDelegate) {
                        [self.captureVideoDataOutput setSampleBufferDelegate:self queue:dispatch_get_current_queue()];
                    }
                }
            }
        }
    }
}


#pragma mark - Samle Buffer Delegate
// 抽样缓存写入时所调用的委托程序
- (void)captureOutput:(AVCaptureOutput *)captureOutput
didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
       fromConnection:(AVCaptureConnection *)connection
{
    UIImage *img = [self imageFromSampleBuffer:sampleBuffer];
    UIImage *image = [[InfoTools shareInfoTools] fixOrientation:img];
    // 人脸检测
    NSArray *features = [[InfoTools shareInfoTools]leftEyePositionsWithImage:image];
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.videoMainView.subviews.count -1 <features.count) {
            FaceLabel *label =[[FaceLabel alloc]init];
            label.hidden = YES;
            [self.videoMainView addSubview:label];
        }
        for (UIView *label in self.videoMainView.subviews) {
            if ([label isMemberOfClass:[FaceLabel class]]) {
                label.hidden = YES;
            }
        }
        if (features.count >0) {
            for (int i=0;i<features.count; i++) {
                NSValue *layerRect = features[i];
                FaceLabel *label = self.videoMainView.subviews[i+1];
                CGRect originalRect = [layerRect CGRectValue];
                CGRect getRect = [self getUIImageViewRectFromCIImageRect:originalRect];
                label.frame = getRect;
                label.hidden = NO;
            }
        }
        else{
            for (UIView *label in self.videoMainView.subviews) {
                if ([label isMemberOfClass:[FaceLabel class]]) {
                    label.hidden = YES;
                }
            }
        }
    });
    
    
    if (isClick) {
        if(features.count<1){
            NSString *title = @"提示";
            NSString *message = @"照片中的人像不清晰，请重新拍摄";
            UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
            [self alertControllerWithTitle:title message:message okAction:okAction cancelAction:nil];
        }else{
            BOOL numberBol  = NO;
//            if ([self.outPutSetting isEqualToNumber:[NSNumber numberWithInt:kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange]] || [self.outPutSetting isEqualToNumber:[NSNumber numberWithInt:kCVPixelFormatType_420YpCbCr8BiPlanarFullRange]]) {
//                CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
//                if (captureOutput ) {
//                    // 身份证信息识别
//                    numberBol = [self numberRecognit:imageBuffer];
//                }
//            } else {
//                NSLog(@"输出格式不支持");
//            }
//            if(numberBol){
                [self.captureSession stopRunning];
                IDInfoViewController *IDInfoVC = [[IDInfoViewController alloc] init];
                IDInfoVC.IDImage = image;// 图像
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.navigationController pushViewController:IDInfoVC animated:YES];
                });
//            }else{
//                NSString *title = @"提示";
//                NSString *message = @"照片中的相关编号不清晰，请重新拍摄";
//                UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
//                [self alertControllerWithTitle:title message:message okAction:okAction cancelAction:nil];
//            }
        }
        [self.captureSession startRunning];
        isClick = 0;
    }
    //    CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
    //    CVBufferRelease(imageBuffer);
}

- (UIImage *)imageFromSampleBuffer:(CMSampleBufferRef) sampleBuffer
{
    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    CIImage *ciImage = [CIImage imageWithCVPixelBuffer:imageBuffer];
    CIContext *temporaryContext = [CIContext contextWithOptions:nil];
    CGImageRef videoImage = [temporaryContext createCGImage:ciImage fromRect:CGRectMake(0, 0, CVPixelBufferGetWidth(imageBuffer), CVPixelBufferGetHeight(imageBuffer))];
    
    UIImage *result = [[UIImage alloc] initWithCGImage:videoImage scale:1.0 orientation:UIImageOrientationRight];
    
    CGImageRelease(videoImage);
    return result;
}

#pragma mark 证书编号识别   0，1，2
-(BOOL)numberRecognit:(CVImageBufferRef)imageBuffer {
    return NO;
}

- (CGRect)getUIImageViewRectFromCIImageRect:(CGRect)originAllRect
{
    CGRect getRect = originAllRect;
    
    float scrSalImageW = 720/SCR_WIDTH;
    float scrSalImageH = 1280/SCR_HEIGHT;
    
    getRect.size.width = originAllRect.size.width/scrSalImageW;
    getRect.size.height = originAllRect.size.height/scrSalImageH;
    
    float hx = self.videoMainView.frame.size.width/720;
    float hy = self.videoMainView.frame.size.height/1280;
    
    getRect.origin.x = originAllRect.origin.x*hx;//*hx
    getRect.origin.y = (self.videoMainView.frame.size.height - originAllRect.origin.y*hy) - getRect.size.height;
    return getRect;
}


#pragma mark - 展示UIAlertController
-(void)alertControllerWithTitle:(NSString *)title message:(NSString *)message okAction:(UIAlertAction *)okAction cancelAction:(UIAlertAction *)cancelAction {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message okAction:okAction cancelAction:cancelAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

/**
 *  改变设备属性的统一操作方法
 *
 *  @param propertyChange 属性改变操作
 */
-(void)changeDeviceProperty:(PropertyChangeBlock)propertyChange
{
    AVCaptureDevice *captureDevice= [self.captureDeviceInput device];
    NSError *error;
    //注意改变设备属性前一定要首先调用lockForConfiguration:调用完之后使用unlockForConfiguration方法解锁
    if ([captureDevice lockForConfiguration:&error])
    {
        propertyChange(captureDevice);
        [captureDevice unlockForConfiguration];
    }else
    {
        NSLog(@"设置设备属性过程发生错误，错误信息：%@",error.localizedDescription);
    }
}



@end
