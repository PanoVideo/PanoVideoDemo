//
//  PanoPoolFloatLayout.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoPoolFloatLayout.h"

@implementation PanoPoolFloatLayout {
    __weak UIView *_floatView;
}

- (void)layoutWithViews:(NSArray<UIView *> *)views layoutInfo:(NSDictionary<PanoMediaInfoKey,id> *)info {
    if (views.count != 2) {
        return;
    }
    [views.firstObject mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(views.firstObject.superview);
    }];
    
    [views.lastObject.superview bringSubviewToFront:views.lastObject];
    
    _floatView = views.lastObject;
    
    [self layoutWithInfo:info];
}

- (void)layoutWithInfo:(NSDictionary<PanoMediaInfoKey,id> *)info {
    if (!_floatView.superview || !_floatView) {
        return;
    }
    BOOL topToolBarHidden = false;
    if (info) {
        topToolBarHidden = [info[PanoTopToolBarState] boolValue];
    }
    UIEdgeInsets insets = [info[PanoFloatViewPostionKey] UIEdgeInsetsValue];
    if (!UIEdgeInsetsEqualToEdgeInsets(insets, UIEdgeInsetsZero)) {
        [_floatView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(117.5, 157.5));
            if (insets.left > 0) {
                make.left.mas_equalTo(_floatView.superview).insets(insets);
            } else if (insets.right > 0){
                make.right.mas_equalTo(_floatView.superview).insets(insets);
            }
            CGFloat top = (!topToolBarHidden ? (isIphoneX() ? 88 : 64) : (isIphoneX() ? 44 : 20)) + 7.5;
            CGFloat bottom = topToolBarHidden ?  (isIphoneX() ? 25 : 7.5 ) : 87.5;
            if (insets.top > 0) {
                if (insets.top <= top) {
                    make.top.mas_equalTo(_floatView.superview).mas_offset(top);
                } else {
                    make.top.mas_equalTo(_floatView.superview).insets(insets);
                }
                
            } else if (insets.bottom > 0) {
                if (insets.bottom < bottom) {
                    make.bottom.mas_equalTo(_floatView.superview).mas_offset(-bottom);
                } else {
                    make.bottom.mas_equalTo(_floatView.superview).insets(insets);
                }
            }
        }];
    }else {
        [_floatView mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(117.5, 157.5));
            CGFloat top = (!topToolBarHidden ? (isIphoneX() ? 88 : 64) : (isIphoneX() ? 44 : 20)) + 7.5;
            make.top.right.mas_equalTo(_floatView.superview).insets(UIEdgeInsetsMake(top, 0, 0, 15));
        }];
    }
}

@end
