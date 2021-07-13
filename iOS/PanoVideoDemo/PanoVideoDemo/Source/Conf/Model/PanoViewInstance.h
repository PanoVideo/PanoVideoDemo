//
//  PanoViewInstance.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PanoUserInfo.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, PanoViewPageLayoutType) {
    PanoViewPageLayout_FullScreen, ///< 全屏布局
    PanoViewPageLayout_Float, ///< 浮动布局
    PanoViewPageLayout_4_Avg, ///< 四路平均布局
};

typedef NS_ENUM(NSInteger, PanoViewInstanceType) {
    PanoViewInstance_Video, ///<  视频
    PanoViewInstance_Whiteboard, ///< 白板
    PanoViewInstance_Desktop, ///< 桌面
};

typedef NS_ENUM(NSInteger, PanoViewInstanceMode) {
    PanoViewInstance_Float, ///< 浮动模式
    PanoViewInstance_Avg, ///<  画廊平均模式
    PanoViewInstance_Max, ///<  大窗口模式
};


/// 抽象一个View，可以是视频，白板，桌面
@interface PanoViewInstance : NSObject

@property (nonatomic, strong, readonly) PanoUserInfo *user;

@property (nonatomic, assign) UInt64 userId;

///  View 的实例类型
@property (nonatomic, assign) PanoViewInstanceType type;

/// View 的实例 显示的大小模式
@property (nonatomic, assign) PanoViewInstanceMode mode;

- (instancetype)initWithUserId:(UInt64)userId type:(PanoViewInstanceType)type;

@end


///  某一页显示的实例
@interface PanoViewPage : NSObject

@property (nonatomic, copy) NSArray<PanoViewInstance *> *instances;

@property (nonatomic, assign) PanoViewPageLayoutType type;

@end


typedef enum PanoViewInstanceType PanoAnnotationType;

@interface PanoAnnotationItem : NSObject

@property (nonatomic, assign) UInt64 userId;

@property (nonatomic, assign) SInt32 streamId;

@property (nonatomic, weak, nullable) id view;

@property (nonatomic, assign) PanoAnnotationType type;

- (instancetype)initWithUserId:(UInt64)userId type:(PanoViewInstanceType)type;

@end

NS_ASSUME_NONNULL_END
