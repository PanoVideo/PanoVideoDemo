//
//  PanoAnnnotationConfig.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoConfig.h"
#import "UIColor+Extension.h"
#import "PanoCallClient.h"

NSString *PanoSettingKey = @"setting";

@implementation PanoConfig

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self reset];
    }
    return self;
}

- (void)setFeatures:(NSDictionary *)features {
    if (!features) return;
    [self setValuesForKeysWithDictionary:features];
}

- (void)setValue:(id)value forUndefinedKey:(NSString *)key {
    
}

- (NSArray<NSString *> *)penColors {
    return @[@"#000000", @"#D4380D", @"#D46B08", @"#D4B106", @"#389E0D",
             @"#7CB305", @"#096DD9", @"#1D39C4", @"#531DAB"];
}

- (void)reset {
    // 白板
    _wbRole = kPanoWBRoleAttendee;
    _wbToolType = kPanoWBToolPath;
    _fillType = kPanoWBFillNone;
    _wbLineWidth = 3;
    _wbColor = [UIColor pano_colorWithHexString:[self penColors][rand() % 9]];
    _wbFontStyle = kPanoWBFontNormal;
    _wbFontSize = 40;
    // 标注
    _annoToolType = kPanoWBToolPath;
    _annoLineWidth = 3;
    _annoColor = _wbColor;
    _annoFontStyle = kPanoWBFontNormal;
    _annoFontSize = 40;
    _videoAnnotation = false;
    _mobileScreen = false;
    _av1 = false;
    
    _shareOption = PanoSingleShare;
}


- (NSArray *)shareOptions {
    return @[
        NSLocalizedString(@"每次只有一位参会者可以共享", nil),
        NSLocalizedString(@"仅主持人可以共享", nil),
    ];
}

/**
 0 : 每次只有一位参会者可以共享, 默认值
 1 : 仅主持人可以共享
 key: setting,  value: { share : 0 };
 */

@end
