//
//  WhiteboardViewController.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UserView.h"
#import "PanoWbTopView.h"

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

@property (strong, nonatomic, readonly) PanoWbTopView *topBarView;

@property (weak, nonatomic) id<WhiteboardViewDelegate> whiteboardViewDelegate;

- (void)dismiss;

@end

