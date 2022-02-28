//
//  PanoAnnnotationConfig.h
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoRtc/PanoRtcEngineKit.h"

NS_ASSUME_NONNULL_BEGIN

/**
 0 : 每次只有一位参会者可以共享, 默认值
 1 : 仅主持人可以共享
 */
typedef NS_ENUM(NSInteger, PanoShareOption) {
    PanoSingleShare = 0, 
    PanoOnlyHostShare = 1,
};

extern NSString *PanoSettingKey;

@interface PanoConfig : NSObject

@property (assign, nonatomic) PanoWBRoleType wbRole;
@property (assign, nonatomic) PanoWBToolType wbToolType;
@property (assign, nonatomic) PanoWBFillType fillType;
@property (assign, nonatomic) UInt32 wbLineWidth;
@property (strong, nonatomic) UIColor * wbColor;
@property (assign, nonatomic) PanoWBFontStyle wbFontStyle;
@property (assign, nonatomic) UInt32 wbFontSize;

@property (assign, nonatomic) PanoWBToolType annoToolType;
@property (assign, nonatomic) UInt32 annoLineWidth;
@property (strong, nonatomic) UIColor * annoColor;
@property (assign, nonatomic) PanoWBFontStyle annoFontStyle;
@property (assign, nonatomic) UInt32 annoFontSize;

@property (assign, nonatomic) NSDictionary *features;
@property (assign, nonatomic, getter=enableVideoAnnotation) BOOL videoAnnotation; ///< 是否允许视频标注
@property (assign, nonatomic, getter=enableShareScreen) BOOL mobileScreen; ///< 能否共享桌面
@property (assign, nonatomic, getter=enableAV1) BOOL av1; ///< 是否启用AV1

@property (assign, nonatomic) PanoShareOption shareOption;

@property (strong, nonatomic) NSArray<NSString *> *shareOptions;

- (NSArray<NSString *> *)penColors;

- (void)reset;

@end

NS_ASSUME_NONNULL_END
