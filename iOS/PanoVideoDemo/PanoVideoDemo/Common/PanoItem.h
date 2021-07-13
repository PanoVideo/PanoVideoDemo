//
//  PanoWhiteBoardItem.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "PanoItemDelegate.h"

NS_ASSUME_NONNULL_BEGIN



@interface PanoItem : NSObject <PanoItemDelegate>

@property (nonatomic, strong, readonly) UIImage *image;

@property (nonatomic, copy, readonly) NSString *title;

@property (nonatomic, copy, readonly) PanoClickBlock_t clickBlock;

- (instancetype)initWithImage:(UIImage *)image title:(NSString *)title;

- (instancetype)initWithImage:(UIImage *)image title:(NSString *)title configBlock:(PanoClickBlock_t)config;

@end

NS_ASSUME_NONNULL_END
