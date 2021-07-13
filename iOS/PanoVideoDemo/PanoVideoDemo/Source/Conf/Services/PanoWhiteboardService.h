//
//  PanoWhiteboardService.h
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoBaseService.h"
#import "PanoUserInfo.h"
#import "PanoItem.h"

NS_ASSUME_NONNULL_BEGIN

@protocol PanoWhiteboardDelegate <NSObject>

- (void)onPresenterDidChanged:(PanoUserInfo *)persenter;

- (void)onDocFilesDidChanged;

@end

@interface PanoWhiteboardService<ObjectType> : PanoBaseService<ObjectType>

@property (nonatomic, strong, readonly) PanoUserInfo *presenter;

@property (nonatomic, weak) id<PanoWhiteboardDelegate> delegate;

@property (nonatomic, strong, readonly) NSMutableArray *whiteboards;


- (void)applyBecomePresenter;

- (BOOL)isPresenter;

- (BOOL)hasPresenter;

- (BOOL)sendMessage:(NSDictionary * _Nonnull)message
             toUser:(UInt64)userId;

- (BOOL)broadcastMessage:(NSDictionary * _Nonnull)message;

- (BOOL)switchToDoc:(NSString * _Nonnull)fileName;

- (NSMutableArray<NSString*> * _Nullable)enumerateFiles;

- (NSMutableArray<NSString*> * _Nullable)fileNames;

- (NSString * _Nullable)getCurrentFileId;

- (NSString * _Nullable)getCurrentFileName;

@end

NS_ASSUME_NONNULL_END
