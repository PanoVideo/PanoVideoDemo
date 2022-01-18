//
//  WhiteboardViewController.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol WhiteboardViewDelegate <NSObject>
@optional

- (void)whiteboardViewDidOpen;
- (void)whiteboardViewWillClose;
- (void)whiteboardViewDidClose;

@end

@interface WhiteboardViewController : UIViewController

@property (weak, nonatomic) id<WhiteboardViewDelegate> whiteboardViewDelegate;

- (void)dismiss;

@end

