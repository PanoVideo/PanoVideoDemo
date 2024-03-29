//
//  PanoWhiteboardService.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoWhiteboardService.h"
#import "PanoCallClient.h"

// 打开关闭白板 Property key: Whiteboard, value: { on : true }
NSString *const PanoWhiteBoardKey = @"Whiteboard";

@interface PanoWhiteboardService () <PanoUserDelegate>
@property (nonatomic, strong) NSMutableArray *whiteboards;
@property (nonatomic, strong) NSDictionary *wbProps;
@end

@implementation PanoWhiteboardService

- (void)setDelegate:(id<PanoWhiteboardDelegate>)delegate {
    _delegate = delegate;
    [self addDelegate:delegate];
}

- (NSMutableArray *)whiteboards {
    if (!_whiteboards) {
        _whiteboards = [NSMutableArray array];
    }
    return _whiteboards;
}

- (BOOL)close {
    return [self setWhiteboardStatus:false];
}

- (BOOL)open {
    return [self setWhiteboardStatus:true];
}

- (BOOL)isOn {
    BOOL on = [self.wbProps[@"on"] boolValue];
    return on;
}

- (BOOL)setWhiteboardStatus:(BOOL)on {
    if (![PanoCallClient.shared.userMgr isHost]) {
        return false;
    }
    return [PanoCallClient.shared.rtcService setProperty:@{@"on" : @(on)} forKey:PanoWhiteBoardKey];
}

- (void)applyBecomePresenter {
    [PanoCallClient.shared.engineKit.whiteboardEngine setRoleType:kPanoWBRoleAdmin];
}

- (BOOL)isPresenter {
    return PanoCallClient.shared.userMgr.hostId == PanoCallClient.shared.userId;
}

- (BOOL)hasPresenter {
    return PanoCallClient.shared.userMgr.hostId != 0;
}

- (BOOL)sendMessage:(NSDictionary *)message toUser:(UInt64)userId {
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:message options:0 error:&error];
    PanoResult result = [PanoCallClient.shared.engineKit.whiteboardEngine sendMessage:data toUser:userId];
    return result = kPanoResultOK;
}

- (BOOL)broadcastMessage:(NSDictionary *)message {
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:message options:0 error:&error];
    PanoResult result = [PanoCallClient.shared.engineKit.whiteboardEngine broadcastMessage:data];
    return result = kPanoResultOK;
}

- (BOOL)switchToDoc:(NSString *)fileName {
    NSUInteger index = [[self fileNames] indexOfObject:fileName];
    NSArray *docfiles = [self enumerateFiles];
    if (index < docfiles.count) {
        PanoResult result = [PanoCallClient.shared.engineKit.whiteboardEngine switchDoc:docfiles[index]];
        return result == kPanoResultOK;
    }
    return false;
}

- (NSMutableArray<NSString *> *)enumerateFiles {
    return [PanoCallClient.shared.engineKit.whiteboardEngine enumerateFiles];
}

- (NSMutableArray<NSString *> *)fileNames {
    NSMutableArray<NSString *> *files = [self enumerateFiles];
    NSMutableArray *mArray = [NSMutableArray array];
    [files enumerateObjectsUsingBlock:^(NSString * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSString *title = idx == 0 ? NSLocalizedString(@"Default Doc", nil) :
        [NSString stringWithFormat:@"%@%lu",NSLocalizedString(@"Doc", nil), (unsigned long)idx];
        [mArray addObject:title];
    }];
    return mArray;
}
- (NSString *)getCurrentFileId {
    return [PanoCallClient.shared.engineKit.whiteboardEngine getCurrentFileId];
}

- (NSString *)getCurrentFileName {
    NSString *fileId = [self getCurrentFileId];
    NSInteger index = [[self enumerateFiles] indexOfObject:fileId];
    return [self.fileNames objectAtIndex:index];
}

- (PanoWhiteboardService<id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>> *)wbService {
    return self;
}

#pragma mark -- PanoRtcWhiteboardDelegate

//视角共享开始通知
- (void)onVisionShareStarted:(UInt64)userId {
    [[self wbService] invokeWithAction:@selector(onVisionShareStarted:) completion:^(id<PanoRtcWhiteboardDelegate,PanoWhiteboardDelegate>  _Nonnull del) {
        [del onVisionShareStarted:userId];
    }];
}

//视角共享停止通知
- (void)onVisionShareStopped:(UInt64)userId {
    [[self wbService] invokeWithAction:@selector(onVisionShareStopped:) completion:^(id<PanoRtcWhiteboardDelegate,PanoWhiteboardDelegate>  _Nonnull del) {
        [del onVisionShareStopped:userId];
    }];
}

- (void)onStatusSynced {
    [[self wbService] invokeWithAction:@selector(onStatusSynced) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onStatusSynced];
    }];
}

- (void)onPageNumberChanged:(PanoWBPageNumber)curPage
             withTotalPages:(UInt32)totalPages {
    [[self wbService] invokeWithAction:@selector(onPageNumberChanged:withTotalPages:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onPageNumberChanged:curPage withTotalPages:totalPages];
    }];
}

- (void)onImageStateChanged:(PanoWBImageState)state
                    withUrl:(NSString * _Nonnull)url {
    [[self wbService] invokeWithAction:@selector(onImageStateChanged:withUrl:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onImageStateChanged:state withUrl:url];
    }];
}

- (void)onViewScaleChanged:(Float32)scale {
    [[self wbService] invokeWithAction:@selector(onViewScaleChanged:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onViewScaleChanged:scale];
    }];
}

- (void)onRoleTypeChanged:(PanoWBRoleType)newRole {
    [[self wbService] invokeWithAction:@selector(onRoleTypeChanged:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onRoleTypeChanged:newRole];
    }];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_delegate onPresenterDidChanged];
    });
}

- (void)onContentUpdated {
    [[self wbService] invokeWithAction:@selector(onContentUpdated) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onContentUpdated];
    }];
}

- (void)onSnapshotComplete:(PanoResult)result
                      name:(NSString* _Nonnull)filename {
    [[self wbService] invokeWithAction:@selector(onSnapshotComplete:name:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onSnapshotComplete:result name:filename];
    }];
}

#pragma mark Message Delegate Methods
- (void)onMessageReceived:(NSData *)message fromUser:(UInt64)userId {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[self wbService] invokeWithAction:@selector(onMessageReceived:fromUser:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
            [del onMessageReceived:message fromUser:userId];
        }];
        NSLog(@"onMessageReceived fromUser:%llu", userId);
    });
}

#pragma mark Doc Delegate Methods

- (void)onAddBackgroundImages:(PanoResult)result
                         file:(NSString* _Nonnull)fileId {
    [[self wbService] invokeWithAction:@selector(onAddBackgroundImages:file:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onAddBackgroundImages:result file:fileId];
    }];
}

- (void)onDocTranscodeStatus:(PanoResult)result
                        file:(NSString* _Nonnull)fileId
                    progress:(UInt32)progress
                   pageCount:(UInt32)count {
    [[self wbService] invokeWithAction:@selector(onDocTranscodeStatus:file:progress:pageCount:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onDocTranscodeStatus:result file:fileId progress:progress pageCount:count];
    }];
}

- (void)onDocCreate:(PanoResult)result
               file:(NSString* _Nonnull)fileId {
    [[self wbService] invokeWithAction:@selector(onDocCreate:file:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onDocCreate:result file:fileId];
    }];
    if (result == kPanoResultOK) {
        [self.whiteboards addObject:fileId];
    }
    [[self wbService] invokeWithAction:@selector(onDocFilesDidChanged) completion:^(id<PanoRtcWhiteboardDelegate,PanoWhiteboardDelegate>  _Nonnull del) {
        [del onDocFilesDidChanged];
    }];
}

- (void)onDocDelete:(PanoResult)result
               file:(NSString* _Nonnull)fileId {
    [[self wbService] invokeWithAction:@selector(onDocDelete:file:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onDocDelete:result file:fileId];
    }];
    [[self wbService] invokeWithAction:@selector(onDocFilesDidChanged) completion:^(id<PanoRtcWhiteboardDelegate,PanoWhiteboardDelegate>  _Nonnull del) {
        [del onDocFilesDidChanged];
    }];
}

- (void)onDocSwitch:(PanoResult)result
               file:(NSString* _Nonnull)fileId {
    [[self wbService] invokeWithAction:@selector(onDocSwitch:file:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onDocSwitch:result file:fileId];
    }];
}

#pragma mark User Delegate Methods

- (void)onUserJoined:(UInt64)userId
            withName:(NSString * _Nullable)userName {
    [[self wbService] invokeWithAction:@selector(onUserJoined:withName:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onUserJoined:userId withName:userName];
    }];
}

- (void)onUserLeft:(UInt64)userId {
    NSLog(@"onUserLeft->>>>: %llu",userId);
    [[self wbService] invokeWithAction:@selector(onUserLeft:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onUserLeft:userId];
    }];
}

#pragma mark -- PanoUserDelegate
- (void)onPresenterDidChanged {
    [[self wbService] invokeWithAction:@selector(onPresenterDidChanged) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
        [del onPresenterDidChanged];
    }];
}

#pragma mark -- PanoRtcDelegate
- (void)onPropertyChanged:(NSDictionary *)value forKey:(NSString *_Nonnull)key {
    if ([key isEqualToString:PanoWhiteBoardKey]) {
        self.wbProps = value;
        [[self wbService] invokeWithAction:@selector(onWhiteboardStatusChanged:) completion:^(id<PanoRtcWhiteboardDelegate, PanoWhiteboardDelegate>  _Nonnull del) {
            [del onWhiteboardStatusChanged:[self isOn]];
        }];
    }
}


@end
