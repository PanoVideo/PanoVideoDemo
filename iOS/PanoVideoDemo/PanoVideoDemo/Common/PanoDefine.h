//
//  PanoDefine.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#ifndef PanoDefine_h
#define PanoDefine_h
#import "UIColor+Extension.h"

typedef NS_ENUM(NSInteger, PanoFontSize) {
    PanoFontSize_Min = 10,
    PanoFontSize_Little = 12,
    PanoFontSize_Medium = 16,
    PanoFontSize_Larger = 18
};

static inline BOOL isIphoneX() {
    if (@available(iOS 11.0, *)) {
        UIEdgeInsets safeInsets = [UIApplication sharedApplication].keyWindow.safeAreaInsets;
        return safeInsets.bottom > 0 && safeInsets.top > 0;
    } else {
        return false;
    }
}
static inline BOOL isiPad() {
    return (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad);
}

static inline UIEdgeInsets pano_safeAreaInset(UIView *view) {
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 110000
    if ([view respondsToSelector:@selector(safeAreaInsets)]) {
        if (@available(iOS 11.0, *)) {
            return [view safeAreaInsets];
        }
    }
#endif
    return UIEdgeInsetsZero;
}

static inline CGFloat statusBarHeight() {
    return isIphoneX() ? 44 : 20;
}

static CGFloat LargeFixSpace = 15.0;

static CGFloat DefaultFixSpace = 7.5;

static CGFloat LittleFixSpace = 5;

static NSString * PanoApplinks = @"www.pano.video";

static inline UIColor* appMainColor() {
    return [UIColor pano_colorWithHexString:@"#333333"];
}

static inline UIColor* appHighlightedColor() {
    return [UIColor pano_colorWithHexString:@"#0899F9"];
}

#endif /* PanoDefine_h */
