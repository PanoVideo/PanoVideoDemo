//
//  PanoPoolFullScreenLayout.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoPoolFullScreenLayout.h"

@implementation PanoPoolFullScreenLayout

- (void)layoutWithViews:(NSArray<UIView *> *)views layoutInfo:(NSDictionary<PanoMediaInfoKey,id> *)info {
    if (views.count != 1) {
        return;
    }
    [views.firstObject mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(views.firstObject.superview);
    }];
}

@end
