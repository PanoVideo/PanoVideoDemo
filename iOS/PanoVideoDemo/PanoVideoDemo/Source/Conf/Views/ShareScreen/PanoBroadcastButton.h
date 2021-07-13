//
//  PanoBroadcastButton.h
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import <UIKit/UIKit.h>
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 110000
    #import <ReplayKit/RPBroadcast.h>
#endif

#if TARGET_IPHONE_SIMULATOR
#define TC_TARGET_IPHONE_SIMULATOR
#endif


NS_ASSUME_NONNULL_BEGIN

#ifndef  TC_TARGET_IPHONE_SIMULATOR
API_AVAILABLE(ios(12.0))
@interface PanoBroadcastButton : RPSystemBroadcastPickerView
#else
@interface PanoBroadcastButton : UIView
#endif

@property(nonatomic, weak, readonly)UIButton *innerBtn;

- (void)showBroadcastView;

@end

NS_ASSUME_NONNULL_END
