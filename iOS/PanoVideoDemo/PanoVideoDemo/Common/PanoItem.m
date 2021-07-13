//
//  PanoWhiteBoardItem.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoItem.h"

@implementation PanoItem


- (instancetype)initWithImage:(UIImage *)image title:(NSString *)title {
    return [self initWithImage:image title:title configBlock:^{}];
}

- (instancetype)initWithImage:(UIImage *)image title:(NSString *)title configBlock:(PanoClickBlock_t)config {
    self = [super init];
    if (self) {
        _image = image;
        _title = title;
        _clickBlock = config;
    }
    return self;
}

@end
