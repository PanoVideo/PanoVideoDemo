//
//  PanoUserInfo.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, PanoUserAudioStatus) {
    PanoUserAudio_None = 1, ///<  未连语音
    PanoUserAudio_Unmute, ///<  语音是打开的状态
    PanoUserAudio_Mute, ///< 语音是静音状态
};

typedef NS_ENUM(NSInteger, PanoUserVideoStatus) {
    PanoUserVideo_None = 1, ///<  未打开视频
    PanoUserVideo_Unmute, ///<  视频是打开的状态
    PanoUserVideo_Mute, ///< 视频是停止状态
};

typedef NS_ENUM(NSInteger, PanoUserScreenStatus) {
    PanoUserScreen_None = 1, ///<  未打开桌面
    PanoUserScreen_Unmute, ///<  桌面是打开的状态
    PanoUserScreen_Mute, ///<  桌面是停止状态
};


/// 用户信息
@interface PanoUserInfo : NSObject <NSCopying>

@property (assign, nonatomic, readonly) UInt64 userId;

@property (copy, nonatomic, readonly) NSString * _Nullable userName;

@property (assign, nonatomic) PanoUserAudioStatus audioStatus;

@property (assign, nonatomic) PanoUserVideoStatus videoStaus;

@property (assign, nonatomic) PanoUserScreenStatus screenStatus;

@property (copy, nonatomic) NSString *os;

- (instancetype)initWithId:(UInt64)userId name:(NSString * _Nullable)userName;

- (instancetype)init NS_UNAVAILABLE;

@end


@interface PanoUserInfo (Add)

@property (copy, nonatomic, readonly) NSString * _Nullable videoUserName;

@end

NS_ASSUME_NONNULL_END
