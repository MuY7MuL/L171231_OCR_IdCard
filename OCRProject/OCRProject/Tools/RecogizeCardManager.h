//
//  RecogizeCardManager.h
//  RecognizeCard
//
//

#import <Foundation/Foundation.h>
@class UIImage;

typedef void (^CompleateBlock)(NSString *text);

@interface RecogizeCardManager : NSObject

/**
 *  初始化一个单例
 *
 *  @return 返回一个RecogizeCardManager的实例对象
 */
+ (instancetype)recognizeCardManager;

/**
 *  根据身份证照片得到身份证号码
 *
 *  @param cardImage 传入的身份证照片
 *  @param compleate 识别完成后的回调
 */
- (void)recognizeCardWithImage:(UIImage *)cardImage compleate:(CompleateBlock)compleate;

- (UIImage *)opencvScanCard:(UIImage *)image;

@end
