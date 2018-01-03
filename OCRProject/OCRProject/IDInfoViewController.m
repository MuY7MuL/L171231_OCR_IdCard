//
//  IDInfoViewController.m
//  IDCardRecognition
//


#import "IDInfoViewController.h"

#import "RecogizeCardManager.h"



@interface IDInfoViewController ()<UIImagePickerControllerDelegate>

@property (strong, nonatomic) IBOutlet UIImageView *IDImageView;
//@property (strong, nonatomic) IBOutlet UILabel *IDNumLabel;

@end

@implementation IDInfoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.navigationItem.title = @"信息";
    
    self.IDImageView.layer.cornerRadius = 8;
    self.IDImageView.layer.masksToBounds = YES;
    
//    self.IDNumLabel.text = _IDInfo.num;
    self.IDImageView.contentMode = UIViewContentModeScaleAspectFit;
    self.IDImageView.image = _IDImage;
    
    if(_typeImg ==3||_typeImg==4){
        [self startParseImg];
    }
    
    if(_typeImg ==6){
        [self startParseImgIDCard];
    }
}




- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - 错误，重新拍摄
- (IBAction)shootAgain:(UIButton *)sender {    
   [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - 正确，下一步
- (IBAction)nextStep:(UIButton *)sender {
    NSLog(@"经用户核对，身份证号码正确，那就进行下一步，比如身份证图像或号码经加密后，传递给后台");
    
    [self startParseImg];
    
}

//身份证检测
-(void)startParseImgIDCard{
    [SVProgressHUD showWithStatus:@"正在识别分析图片中...."];
//    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[RecogizeCardManager recognizeCardManager] recognizeCardWithImage:self.IDImageView.image compleate:^(NSString *text) {
            
            if (text != nil) {
                
                [SVProgressHUD dismiss];
                UIAlertView *alert=[[UIAlertView alloc]initWithTitle:@"分析结果" message:[NSString stringWithFormat:@"\n识别结果：%@",text] delegate:self cancelButtonTitle:@"确认" otherButtonTitles:nil];
                [alert show];
                NSLog(@"%@",text);
//                self.textLabel.text = [NSString stringWithFormat:@"识别结果：%@",text];
            }else {
              [SVProgressHUD dismiss];
                NSLog(@"false======");
//                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"提示" message:@"照片识别失败，请选择清晰、没有复杂背景的身份证照片重试！" delegate:self cancelButtonTitle:@"知道了" otherButtonTitles: nil];
//                [alert show];
            }
        }];
        
//    });
    
   
}

-(void)startParseImg{
    [SVProgressHUD showWithStatus:@"正在识别分析图片中...."];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        //OCR图片识别
        //        G8RecognitionOperation  *operation=[[G8RecognitionOperation alloc] initWithLanguage:@"chi_sim"];//中文
        G8RecognitionOperation  *operation=[[G8RecognitionOperation alloc] initWithLanguage:@"eng"];//英文
        //        G8RecognitionOperation  *operation=[[G8RecognitionOperation alloc] initWithLanguage:@"chi_sim+eng"];//中文与英文
        // Configure inner G8Tesseract object as described before
        operation.tesseract.image = [self.IDImageView.image g8_blackAndWhite];
        
        // Setup the recognitionCompleteBlock to receive the Tesseract object
        // after text recognition. It will hold the recognized text.
        operation.recognitionCompleteBlock = ^(G8Tesseract *recognizedTesseract) {
            // Retrieve the recognized text upon completion
            [SVProgressHUD dismiss];
            NSString *tt = @"";
            tt= [recognizedTesseract recognizedText];
            NSString* s2=[tt stringByReplacingOccurrencesOfString:@"\n" withString:@""];
            NSLog(@"========%@ ===%d",[self findNumFromStr:s2],[self findNumFromStr:s2].length);
            
            if(_typeImg ==3 && [self findNumFromStr:s2].length < 40){
                UIAlertView *alert=[[UIAlertView alloc]initWithTitle:@"分析结果" message:[NSString stringWithFormat:@"\n图片格式不符合要求，请重新拍摄"] delegate:self cancelButtonTitle:@"确认" otherButtonTitles:nil];
                [alert show];
            }else if(_typeImg==4 && [self findNumFromStr:s2].length < 35){
                UIAlertView *alert=[[UIAlertView alloc]initWithTitle:@"分析结果" message:[NSString stringWithFormat:@"\n图片格式不符合要求，请重新拍摄"] delegate:self cancelButtonTitle:@"确认" otherButtonTitles:nil];
                [alert show];
            }else{
                UIAlertView *alert=[[UIAlertView alloc]initWithTitle:@"分析结果" message:[NSString stringWithFormat:@"\n图片符合要求"] delegate:self cancelButtonTitle:@"确认" otherButtonTitles:nil];
                [alert show];
            }
            
        };
        // Add operation to queue
        NSOperationQueue *queue = [[NSOperationQueue alloc] init];
        [queue addOperation:operation];
    });
}

-(NSString *)findNumFromStr:(NSString *)needFindStr
{
    NSString *originalString = needFindStr;
    
    // Intermediate
    NSMutableString *numberString = [[NSMutableString alloc] init] ;
    NSString *tempStr;
    NSScanner *scanner = [NSScanner scannerWithString:originalString];
    NSCharacterSet *numbers = [NSCharacterSet characterSetWithCharactersInString:@"0123456789"];
    
    while (![scanner isAtEnd]) {
        // Throw away characters before the first number.
        [scanner scanUpToCharactersFromSet:numbers intoString:NULL];
        
        // Collect numbers.
        [scanner scanCharactersFromSet:numbers intoString:&tempStr];
        [numberString appendString:tempStr];
        tempStr = @"";
    }
    
    return [NSString stringWithString:numberString];
}




@end
