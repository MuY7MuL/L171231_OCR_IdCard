//
//  RecogizeCardManager.m
//  RecognizeCard
//
//

#import "RecogizeCardManager.h"
#import <opencv2/opencv.hpp>
#import <opencv2/imgproc/types_c.h>
#import <opencv2/imgcodecs/ios.h>
#import "TesseractOCR.h"

@implementation RecogizeCardManager

+ (instancetype)recognizeCardManager {
    static RecogizeCardManager *recognizeCardManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        recognizeCardManager = [[RecogizeCardManager alloc] init];
    });
    return recognizeCardManager;
}

- (void)recognizeCardWithImage:(UIImage *)cardImage compleate:(CompleateBlock)compleate {
    //扫描身份证图片，并进行预处理，定位号码区域图片并返回
    UIImage *numberImage = [self opencvScanCard2:cardImage];
    if (numberImage == nil) {
        compleate(nil);
    }
    //利用TesseractOCR识别文字
    [self tesseractRecognizeImage:numberImage compleate:^(NSString *numbaerText) {
        compleate(numbaerText);
    }];
}


//扫描身份证图片，并进行预处理，定位号码区域图片并返回
- (UIImage *)opencvScanCard:(UIImage *)image {
    
    //将UIImage转换成Mat
    cv::Mat resultImage;
    UIImageToMat(image, resultImage);
    //转为灰度图
    cvtColor(resultImage, resultImage, cv::COLOR_BGR2GRAY);
    //利用阈值二值化
    cv::threshold(resultImage, resultImage, 100, 255, CV_THRESH_BINARY);
    //腐蚀，填充（腐蚀是让黑色点变大）
    cv::Mat erodeElement = getStructuringElement(cv::MORPH_RECT, cv::Size(26,26));
    cv::erode(resultImage, resultImage, erodeElement);
    //轮廊检测
    std::vector<std::vector<cv::Point>> contours;//定义一个容器来存储所有检测到的轮廊
    cv::findContours(resultImage, contours, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
    //取出身份证号码区域
    std::vector<cv::Rect> rects;
    cv::Rect numberRect = cv::Rect(0,0,0,0);
    std::vector<std::vector<cv::Point>>::const_iterator itContours = contours.begin();
    for ( ; itContours != contours.end(); ++itContours) {
        cv::Rect rect = cv::boundingRect(*itContours);
        rects.push_back(rect);
        //算法原理
        if (rect.width > numberRect.width && rect.width > rect.height * 5) {
            numberRect = rect;
        }
       
    }
    //身份证号码定位失败
    if (numberRect.width == 0 || numberRect.height == 0) {
        return nil;
    }
    //定位成功成功，去原图截取身份证号码区域，并转换成灰度图、进行二值化处理
    cv::Mat matImage;
    UIImageToMat(image, matImage);
    resultImage = matImage(numberRect);
    cvtColor(resultImage, resultImage, cv::COLOR_BGR2GRAY);
    cv::threshold(resultImage, resultImage, 80, 255, CV_THRESH_BINARY);
    //将Mat转换成UIImage
    UIImage *numberImage = MatToUIImage(resultImage);
    
    
    return numberImage;
}

- (UIImage*)opencvScanCard2:(UIImage*)image {
    
    //将UIImage转换成Mat
    cv::Mat resultImage;
    UIImageToMat(image, resultImage);
    //先用使用 3x3内核来降噪
    blur( resultImage, resultImage, cv::Size(3,3),cv::Point(-1,-1));
    //积分阈值二值化
    resultImage =AdaptiveThereshold(resultImage, resultImage);
    //腐蚀，填充（腐蚀是让黑色点变大）
    cv::Mat erodeElement = getStructuringElement(cv::MORPH_RECT, cv::Size(22,22));
    cv::erode(resultImage, resultImage, erodeElement);
    //轮廊检测
    std::vector<std::vector<cv::Point>> contours;//定义一个容器来存储所有检测到的轮廊
    cv::findContours(resultImage, contours, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
    //cv::drawContours(resultImage, contours, -1, cv::Scalar(255),4);
    //取出身份证号码区域
    std::vector<cv::Rect> rects;
    cv::Rect NameNumberRect = cv::Rect(0,0,0,0);
    cv::Rect CardNumberRect = cv::Rect(0,0,0,0);
    
    cv::Rect CardNumberRect1 = cv::Rect(0,0,0,0);
    cv::Rect CardNumberRect2 = cv::Rect(0,0,0,0);
    cv::Rect CardNumberRect3 = cv::Rect(0,0,0,0);
   
    std::vector<std::vector<cv::Point>>::const_iterator itContours = contours.begin();
    for ( ; itContours != contours.end(); ++itContours) {
        cv::Rect rect = cv::boundingRect(*itContours);
        rects.push_back(rect);
        //算法原理
        if (rect.y > 1300 && rect.width > CardNumberRect.width && rect.width > rect.height * 6) {
             NSLog(@"==%d==%d",rect.y,rect.x);
            CardNumberRect = rect;
        }
        if (rect.y > 1100 && rect.y < 1300 && rect.width > CardNumberRect.width && rect.width > rect.height * 6) {
            NSLog(@"==%d==%d",rect.y,rect.x);
            CardNumberRect1 = rect;
        }
        if (rect.y > 1000 && rect.y < 1100 && rect.width > CardNumberRect.width && rect.width > rect.height * 6) {
            NSLog(@"==%d==%d",rect.y,rect.x);
            CardNumberRect2 = rect;
        }
        if (rect.y < 1000 && rect.width > CardNumberRect.width && rect.width > rect.height * 6) {
            NSLog(@"==%d==%d",rect.y,rect.x);
            CardNumberRect3 = rect;
        }
        if (rect.x > 100 && rect.y<100 && rect.height < rect.width) {
             NSLog(@"%d==%d",rect.y,rect.x);
            NameNumberRect = rect;
        }
    }
    
    
    //定位成功成功，去原图截取身份证号码区域，并转换成灰度图、进行二值化处理
    //去原图截取身份证姓名区域，并转换成灰度图、进行二值化处理
    cv::Mat matImage;
    UIImageToMat(image, matImage);
    cv::Mat NameImageMat;
    NameImageMat = matImage(NameNumberRect);
    IplImage grey = NameImageMat;
    unsigned char* dataImage = (unsigned char*)grey.imageData;
//    int threshold = Otsu(dataImage, grey.width, grey.height);
//    printf("阈值：%d\n",threshold);
    NameImageMat =AdaptiveThereshold(NameImageMat, NameImageMat);
    UIImage *NameImage = MatToUIImage(NameImageMat);
    
    cv::Mat CardImageMat;
    CardImageMat = matImage(CardNumberRect);
    CardImageMat =AdaptiveThereshold(CardImageMat, CardImageMat);
    UIImage *CardImage = MatToUIImage(CardImageMat);
    
    //身份证号码定位失败
//    if ([publicClass objectIsEmpty:CardImage]||[publicClass objectIsEmpty:NameImage]) {
//        return nil;
//    }
   // NSArray* arr = @[NameImage,CardImage];
    return CardImage;
    
}

cv::Mat AdaptiveThereshold(cv::Mat src,cv::Mat dst)
{
    cvtColor(src,dst,CV_BGR2GRAY);
    int x1, y1, x2, y2;
    int count=0;
    long long sum=0;
    int S=src.rows>>3;  //划分区域的大小S*S
    int T=15;         /*百分比，用来最后与阈值的比较。原文：If the value of the current pixel is t percent less than this average
                       then it is set to black, otherwise it is set to white.*/
    int W=dst.cols;
    int H=dst.rows;
    long long **Argv;
    Argv=new long long*[dst.rows];
    for(int ii=0;ii<dst.rows;ii++)
    {
        Argv[ii]=new long long[dst.cols];
    }
    
    for(int i=0;i<W;i++)
    {
        sum=0;
        for(int j=0;j<H;j++)
        {
            sum+=dst.at<uchar>(j,i);
            if(i==0)
                Argv[j][i]=sum;
            else
                Argv[j][i]=Argv[j][i-1]+sum;
        }
    }
    
    for(int i=0;i<W;i++)
    {
        for(int j=0;j<H;j++)
        {
            x1=i-S/2;
            x2=i+S/2;
            y1=j-S/2;
            y2=j+S/2;
            if(x1<0)
                x1=0;
            if(x2>=W)
                x2=W-1;
            if(y1<0)
                y1=0;
            if(y2>=H)
                y2=H-1;
            count=(x2-x1)*(y2-y1);
            sum=Argv[y2][x2]-Argv[y1][x2]-Argv[y2][x1]+Argv[y1][x1];
            
            
            if((long long)(dst.at<uchar>(j,i)*count)<(long long)sum*(100-T)/100)
                dst.at<uchar>(j,i)=0;
            else
                dst.at<uchar>(j,i)=255;
        }
    }
    for (int i = 0 ; i < dst.rows; ++i)
    {
        delete [] Argv[i];
    }
    delete [] Argv;
    return dst;
}


//利用TesseractOCR识别文字
- (void)tesseractRecognizeImage:(UIImage *)image compleate:(CompleateBlock)compleate {
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        G8Tesseract *tesseract = [[G8Tesseract alloc] initWithLanguage:@"eng"];
        tesseract.image = [image g8_blackAndWhite];
        tesseract.image = image;
        // Start the recognition
        [tesseract recognize];
        //执行回调
        compleate(tesseract.recognizedText);
    });
}

@end
