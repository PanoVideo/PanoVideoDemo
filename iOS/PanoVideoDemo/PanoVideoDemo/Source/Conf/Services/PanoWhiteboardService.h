//
//  PanoWhiteboardService.h
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoUserInfo.h"

NS_ASSUME_NONNULL_BEGIN

@protocol PanoWhiteboardDelegate <NSObject>

@optional
/// 通知演示者发生改变
/// @param persenter 演示者
- (void)onPresenterDidChanged;

/// 通知白板文档发生改变
- (void)onDocFilesDidChanged;

/// 通知白板打开或者关闭
- (void)onWhiteboardStatusChanged:(BOOL)on;

@end

/// 白板核心接口
@interface PanoWhiteboardService<T> : PanoBaseService<T>

@property (nonatomic, weak) id<PanoWhiteboardDelegate> delegate;

@property (nonatomic, strong, readonly) NSMutableArray *whiteboards;


/// 主动关闭白板
- (BOOL)close;

/// 主动打开白板
- (BOOL)open;

/// 白板是否打开
- (BOOL)isOn;

/// 申请成为演示者
- (void)applyBecomePresenter;

/// 自己是否是演示者
- (BOOL)isPresenter;

/// 是否有演示者
- (BOOL)hasPresenter;

/// 通过白板通道发送即时消息
- (BOOL)sendMessage:(NSDictionary * _Nonnull)message
             toUser:(UInt64)userId;

/// 通过白板通道广播消息
- (BOOL)broadcastMessage:(NSDictionary * _Nonnull)message;

/// 切换白板文档
- (BOOL)switchToDoc:(NSString * _Nonnull)fileName;

/// 获取到所有的文档
- (NSMutableArray<NSString*> * _Nullable)enumerateFiles;

/// 获取到所有的文档名称
- (NSMutableArray<NSString*> * _Nullable)fileNames;

/// 获取当前文档ID
- (NSString * _Nullable)getCurrentFileId;

/// 获取当前文档名称
- (NSString * _Nullable)getCurrentFileName;

@end

NS_ASSUME_NONNULL_END
