//
//  PanoAction.h
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class PanoAction;

typedef void (^TCActionHandler)(PanoAction *action);

@interface PanoAction : NSObject

@property (nonatomic, strong) TCActionHandler handler;
@property (nonatomic, strong) UIImage *imgIcon;
@property (nonatomic, strong) UIImage *selectedIcon;
@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) UIColor *titleColor;

- (id)initWithTitle:(nullable NSString *)title
           imgIcon:(nullable UIImage *)image
            handler:(TCActionHandler)handler;

- (id)initWithTitle:(nullable NSString *)title
           imgIcon:(nullable UIImage *)image
       selectedIcon:(nullable UIImage *)selectedImage
            handler:(TCActionHandler)handler;
@end


NS_ASSUME_NONNULL_END
