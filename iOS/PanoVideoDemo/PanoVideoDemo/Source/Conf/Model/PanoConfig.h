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

- (NSArray<NSString *> *)penColors;

@end

NS_ASSUME_NONNULL_END
