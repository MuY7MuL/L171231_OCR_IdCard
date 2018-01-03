//
//  ViewController.m
//  OCRProject
//
//  Created by Lin Yan on 2017/10/24.
//  Copyright © 2017年 Lin Yan. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()
@property(nonatomic,retain) UIImage *parseImg;
@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.mButton.layer.backgroundColor=[UIColor orangeColor].CGColor;
    self.mButton.layer.cornerRadius=5;
    [self.mButton addTarget:self action:@selector(startParseImg) forControlEvents:UIControlEventTouchUpInside];
    //设置等待框样式
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    self.parseImg=[UIImage imageNamed:@"cerit"];//英文图片
    self.mImageView.image=self.parseImg;
}


-(void)startParseImg{
    [SVProgressHUD showWithStatus:@"正在识别分析图片中...."];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        G8RecognitionOperation  *operation=[[G8RecognitionOperation alloc] initWithLanguage:@"eng"];//英文
        operation.tesseract.image = [self.parseImg g8_blackAndWhite];
        operation.recognitionCompleteBlock = ^(G8Tesseract *recognizedTesseract) {
            [SVProgressHUD dismiss];
            NSString *tt = @"";
            tt= [recognizedTesseract recognizedText];
            NSString* s2=[tt stringByReplacingOccurrencesOfString:@"\n" withString:@""];
            NSLog(@"========%@ ",[self findNumFromStr:s2]);
            
            UIAlertView *alert=[[UIAlertView alloc]initWithTitle:@"分析结果" message:[NSString stringWithFormat:@"\n%@",[self findNumFromStr:s2]] delegate:self cancelButtonTitle:@"确认" otherButtonTitles:nil];
            [alert show];
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

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


@end
