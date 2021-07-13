//
//  PanoAnnnotationConfig.m
//  PanoVideoDemo
//
//  
//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoConfig.h"
#import "UIColor+Extension.h"

@implementation PanoConfig

- (instancetype)init
{
    self = [super init];
    if (self) {
        // 白板
        _wbRole = kPanoWBRoleAttendee;
        _wbToolType = kPanoWBToolPath;
        _fillType = kPanoWBFillNone;
        _wbLineWidth = 5;
        _wbColor = [UIColor pano_colorWithHexString:[self penColors][rand() % 9]];
        _wbFontStyle = kPanoWBFontNormal;
        _wbFontSize = 10;
        // 标注
        _annoToolType = kPanoWBToolPath;
        _annoLineWidth = 5;
        _annoColor = _wbColor;
        _annoFontStyle = kPanoWBFontNormal;
        _annoFontSize = 40;
    }
    return self;
}

- (NSArray<NSString *> *)penColors {
    return @[@"#000000", @"#D4380D", @"#D46B08", @"#D4B106", @"#389E0D",
             @"#7CB305", @"#096DD9", @"#1D39C4", @"#531DAB"];
}

@end
