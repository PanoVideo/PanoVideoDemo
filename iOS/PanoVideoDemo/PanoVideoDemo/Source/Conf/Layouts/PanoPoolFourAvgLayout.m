//
//  PanoPoolFourAvgLayout.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoPoolFourAvgLayout.h"
#import "Masonry.h"

static const CGFloat kSpacing = 0;
static const CGFloat kFixedSpacing = 1.5;

@implementation PanoPoolFourAvgLayout

- (void)layoutWithViews:(NSArray<UIView *> *)views layoutInfo:(NSDictionary<PanoMediaInfoKey,id> *)info {
    if (!(views.count == 4 || views.count == 3 || views.count == 2)) {
        return;
    }
    
    // 1. 清除约束
    for (UIView *view in views) {
        [view mas_remakeConstraints:^(MASConstraintMaker *make) {
        }];
    }
    
    NSArray<UIView *>*fisrtArray = [views subarrayWithRange:NSMakeRange(0, 2)];
    [fisrtArray mas_distributeViewsAlongAxis:MASAxisTypeHorizontal withFixedSpacing:kFixedSpacing leadSpacing:kSpacing tailSpacing:kSpacing];
    
    for (NSUInteger i=0; i<fisrtArray.count; i++) {
        UIView *view = fisrtArray[i];
        [view mas_updateConstraints:^(MASConstraintMaker *make) {
            make.top.mas_equalTo(view.superview);
            if (views.count == 2 && i == 0) {
                make.height.mas_equalTo(view.superview).multipliedBy(0.5);
            }
            if (i==1) {
                make.height.mas_equalTo(fisrtArray.firstObject);
            }
        }];
    }
    
    NSArray<UIView *>*secondArray = [views subarrayWithRange:NSMakeRange(2, views.count-2)];
    if (secondArray.count == 2) {
        [secondArray mas_distributeViewsAlongAxis:MASAxisTypeHorizontal withFixedSpacing:kFixedSpacing leadSpacing:kSpacing tailSpacing:kSpacing];
    }
    for (UIView *view in secondArray) {
        [view mas_updateConstraints:^(MASConstraintMaker *make) {
            make.top.mas_equalTo(fisrtArray.firstObject.mas_bottom).mas_offset(kFixedSpacing);
            make.height.mas_equalTo(fisrtArray.firstObject);
            make.bottom.mas_equalTo(view.superview);
            if (secondArray.count == 1) {
                make.left.width.mas_equalTo(fisrtArray.firstObject);
            }
        }];
    }
}


@end
