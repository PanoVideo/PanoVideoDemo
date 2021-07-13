//
//  PanoAction.m
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoAction.h"

@implementation PanoAction

- (id)initWithTitle:(nullable NSString *)title
           imgIcon:(nullable UIImage *)image
            handler:(TCActionHandler)handler {
    return [self initWithTitle:title imgIcon:image selectedIcon:nil handler:handler];
}

- (id)initWithTitle:(nullable NSString *)title
           imgIcon:(nullable UIImage *)image
       selectedIcon:(nullable UIImage *)selectedImage
            handler:(TCActionHandler)handler {
    self = [super init];
    if (self) {
        self.handler = handler;
        self.imgIcon = image;
        self.title = title;
        self.selectedIcon = selectedImage;
    }
    return self;
}

@end
