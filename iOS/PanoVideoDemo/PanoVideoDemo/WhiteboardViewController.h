//
//  WhiteboardViewController.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserView.h"

@protocol WhiteboardViewDelegate <NSObject>
@optional

- (void)whiteboardViewDidOpen;
- (void)whiteboardViewWillClose;
- (void)whiteboardViewDidClose;

@end

@interface WhiteboardViewController : UIViewController

@property (strong, nonatomic) IBOutlet UserView * user1View;
@property (strong, nonatomic) IBOutlet UserView * user2View;
@property (strong, nonatomic) IBOutlet UserView * user3View;
@property (strong, nonatomic) IBOutlet UserView * user4View;

@property (weak, nonatomic) id<WhiteboardViewDelegate> whiteboardViewDelegate;

@end

