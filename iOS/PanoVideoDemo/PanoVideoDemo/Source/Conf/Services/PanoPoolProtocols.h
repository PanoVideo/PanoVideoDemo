//
//  PanoPoolProtocols.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NSUInteger PageIndex;

@protocol PanoTurnPageDelegate <NSObject>

- (void)fetchNextPage;

- (void)fetchLastPage;

- (void)switchToPage:(PageIndex)index;

- (BOOL)enableFetchNextPage;

- (BOOL)enableFetchLastPage;

@end


@class PanoViewPage;

@protocol PanoPoolActiveAudioDelegate <NSObject>

@optional
/// 语音激励用户发生改变， 一秒回调一次
/// @param instance  被激励的用户，如果当前没有用户激励，instance 返回 nil
/// @param activing
- (void)onAudioActiveUserChanged:(PanoViewInstance *)instance activing:(BOOL)activing;


/// 语音激励用户的音视频状态发生了改变
/// @param instance 被激励的用户
- (void)onAudioActiveUserStatusChanged:(PanoViewInstance *)instance;

/// 自己的音频是否活跃回调
- (void)onMyAudioActiveChanged:(BOOL)activing;

/// 静音状态下监测自己在说话
- (void)onSpeakingWhenTheAudioMuted;

@end

@protocol PanoRoleDelegate <NSObject>
@optional
- (void)onMyRoleBecomeViewer;

@end

@protocol PanoAnnotationDelegate <PanoRoleDelegate>

@optional
- (void)onAnnotationStart:(PanoAnnotationItem *)item;

- (void)onAnnotationStop:(PanoAnnotationItem *)item;

@end

@protocol PanoPoolDelegate <PanoPoolActiveAudioDelegate, PanoAnnotationDelegate>

- (void)onPoolMediaChanged:(PanoViewPage *)page;

@end

NS_ASSUME_NONNULL_END
