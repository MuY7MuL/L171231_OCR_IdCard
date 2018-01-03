//
//  FirstViewController.m
//  EYE_ImageCollect
//
//  Created by LinYan on 06/12/2017.
//  Copyright © 2017 Dennis Gao. All rights reserved.
//

#import "FirstViewController.h"
#import "ZXViewController.h"
#import "IDCardUpViewController.h"
#import "IDCardDownViewController.h"
//
#import "CreditViewController.h"
//
#import "IDInfoViewController.h"

@interface FirstViewController ()<UINavigationControllerDelegate,UIImagePickerControllerDelegate>

@end

@implementation FirstViewController

-(instancetype)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        //[self customBackBarButtonItem];
    }
    
    return self;
}

#pragma mark - 自定义系统自带的backBarButtomItem
// 去掉系统默认自带的文字（上一个控制器的title），修改系统默认的样式（一个蓝色的左箭头）为自己的图片
-(void)customBackBarButtonItem {
    // 去掉文字
    // 自定义全局的barButtonItem外观
    UIBarButtonItem *barButtonItemAppearance = [UIBarButtonItem appearance];
    // 将文字减小并设其颜色为透明以隐藏
    [barButtonItemAppearance setTitleTextAttributes:@{NSFontAttributeName:[UIFont systemFontOfSize:0.1], NSForegroundColorAttributeName: [UIColor clearColor]} forState:UIControlStateNormal];
    // 获取全局的navigationBar外观
    UINavigationBar *navigationBarAppearance = [UINavigationBar appearance];
    // 获取原图
    UIImage *image = [[UIImage imageNamed:@"nav_back"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    // 修改navigationBar上的返回按钮的图片，注意：这两个属性要同时设置
    navigationBarAppearance.backIndicatorImage = image;
    navigationBarAppearance.backIndicatorTransitionMaskImage = image;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = @"身份识别";
    self.navigationController.delegate = self;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - 导航控制器代理方法
#pragma mark 导航控制器即将展示新的控制器时，会掉用这个方法
// 要想使用该方法，必须1、控制器遵循UINavigationControllerDelegate；2、控制器代理必须为遵循UINavigationControllerDelegate控制器
-(void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated {
    // 将所有即将展示的控制器的leftBarButtonItem设置为左箭头
    // 获得导航控制器的根控制器(栈底控制器)
    UIViewController *rootVC = navigationController.viewControllers[0];
    
    if (![viewController isEqual:rootVC]) {// 如果即将展示的控制器不是导航控制器的根控制器
        viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[[UIImage imageNamed:@"nav_back"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] style:UIBarButtonItemStylePlain target:self action:@selector(back)];
    }
}

-(void)back {
    // 跳到上一级控制器,self就代表导航控制器NavigationVC
    [self.navigationController popViewControllerAnimated:YES];
}

//手持身份证
- (IBAction)onHandClick:(UIButton *)sender {
    ZXViewController *IDInfoVC = [[ZXViewController alloc] init];
    //    IDInfoVC.IDImage = image;// 图像
    NSLog(@"hand......");
    [self.navigationController pushViewController:IDInfoVC animated:YES];
}

//征信，身份证识别 up
- (IBAction)idCardClick:(UIButton *)sender {
    IDCardUpViewController  *idCardVC = [[IDCardUpViewController alloc] init];
    [self.navigationController pushViewController:idCardVC animated:YES];
}

//征信，身份证识别 down
- (IBAction)idCardDownClick:(UIButton *)sender {    
    IDCardDownViewController  *idCardDown = [[IDCardDownViewController alloc] init];
    [self.navigationController pushViewController:idCardDown animated:YES];
}

//征信 a
- (IBAction)pagerClickAClick:(UIButton *)sender {
    CreditViewController  *CreditScan = [[CreditViewController alloc] init];
    CreditScan.typeImg = 3;
    [self.navigationController pushViewController:CreditScan animated:YES];
}

//征信 b

- (IBAction)pagerBClick:(UIButton *)sender {
    CreditViewController  *CreditScan = [[CreditViewController alloc] init];
    CreditScan.typeImg = 4;
    [self.navigationController pushViewController:CreditScan animated:YES];
}
- (IBAction)imageGo:(UIButton *)sender {
   
//    UIImage *img = [UIImage imageNamed:@"cerit"];
//    IDInfoViewController *idInfo = [[IDInfoViewController alloc] init];
//    idInfo.IDImage = img;
//    [self.navigationController pushViewController:idInfo animated:YES];
    
    [self uploadPic:1];
    
}

- (void)uploadPic:(NSInteger)type{
//    self.type = type;
//    self.imageAllowEditing = type == -1;
    //    self.imageAllowEditing = YES;
//    NSLog(@"uploadPic: type = %ld", (long)self.type);
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"选择方式" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
        __weak typeof(self) weakSelf = self;
    // 判断是否支持相机
    if([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]){
        UIAlertAction *cameraAction = [UIAlertAction actionWithTitle:@"拍照" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            if (type > -1) {
                
            } else {
//                self.overlayRotated = NO;
                
                // 跳转到相机或相册页面
                UIImagePickerController *imagePickerController = [[UIImagePickerController alloc] init];
                //UIImagePickerController协议
                imagePickerController.delegate = self;
                //允许编辑
//                imagePickerController.allowsEditing = self.imageAllowEditing;
                imagePickerController.sourceType = UIImagePickerControllerSourceTypeCamera;
                //模态到相机或相册页面
                [self presentViewController:imagePickerController animated:YES completion:nil];
            }
        }];
        [alertController addAction:cameraAction];
    }
    
    UIAlertAction *photoAction = [UIAlertAction actionWithTitle:@"从相册选取" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        // 跳转到相机或相册页面
        UIImagePickerController *imagePickerController = [[UIImagePickerController alloc] init];
        //UIImagePickerController协议
        imagePickerController.delegate = self;
        //允许编辑
//        imagePickerController.allowsEditing = self.imageAllowEditing;
        imagePickerController.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        //模态到相机或相册页面
        [self presentViewController:imagePickerController animated:YES completion:nil];
    }];
    [alertController addAction:photoAction];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    [alertController addAction:cancelAction];
    
    [self presentViewController:alertController animated:YES completion:nil];
}


- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info{
       //获取图片
         UIImage *image = info[UIImagePickerControllerOriginalImage];
        [self dismissViewControllerAnimated:YES completion:nil];
//         myImageView.image = image;
    
        IDInfoViewController *idInfo = [[IDInfoViewController alloc] init];
        idInfo.IDImage = image;
        idInfo.typeImg = 3;
        [self.navigationController pushViewController:idInfo animated:YES];
}

@end
