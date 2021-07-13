//
//  PanoNotificationCenter.m
//  PanoVideoDemo
//

//  Copyright © 2021 Pano. All rights reserved.
//

#import "PanoNotificationCenter.h"

static NSString * const kPanoNotificationName = @"kPanoNotificationName";
static NSString * const kPanoNotificationIdentifier = @"NotificationIdentifier";
static NSString * const kPanoNotificationIdentifierUserInfo = @"NotificationIdentifierUserInfo";

@interface PanoNotificationCenter ()
@property (strong, nonatomic) NSMutableDictionary *listenerBlocks;
@end

@implementation PanoNotificationCenter

+ (instancetype)defaultCenter {
    static PanoNotificationCenter *center = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        center = [[PanoNotificationCenter alloc] init];
    });
    return center;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.listenerBlocks = [NSMutableDictionary dictionary];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didReceiveMessageNotification:) name:kPanoNotificationName object:nil];
    }
    return self;
}

- (void)listenWithIdentifier:(NSString *)identifier listener:(void (^)(id __nullable messageObject))listener
{
  if (identifier)
  {
    [self.listenerBlocks setObject:listener forKey:identifier];
    [self registerForNotificationsWithIdentifier:identifier];
  }
}

- (void)stopListeningWithIdentifier:(NSString *)identifier
{
  if (identifier)
  {
    [self.listenerBlocks removeObjectForKey:identifier];
    [self unregisterForNotificationsWithIdentifier:identifier];
  }
}

- (void)sendNotificationWithIdentifier:(NSString *)identifier
                              userInfo:(NSDictionary *)userInfo {
    CFNotificationCenterRef const center = CFNotificationCenterGetDarwinNotifyCenter();
    BOOL const deliverImmediately = YES;
    CFStringRef str = (__bridge CFStringRef)identifier;
    CFNotificationCenterPostNotification(center, str, NULL, (CFDictionaryRef)userInfo, deliverImmediately);
}

- (void)unregisterForNotificationsWithIdentifier:(NSString *)identifier
{
    CFNotificationCenterRef const center = CFNotificationCenterGetDarwinNotifyCenter();
    CFStringRef str = (__bridge CFStringRef)identifier;
    CFNotificationCenterRemoveObserver(center, (__bridge const void *)(self), str, NULL);
}

- (void)didReceiveMessageNotification:(NSNotification *)notification
{
    typedef void (^MessageListenerBlock)(id messageObject);

    NSDictionary *userInfo = notification.userInfo;
    NSString *identifier = [userInfo valueForKey:kPanoNotificationIdentifier];
    NSString *info = [userInfo valueForKey:kPanoNotificationIdentifierUserInfo];
    if (identifier)
    {
        MessageListenerBlock listenerBlock = [self listenerBlockForIdentifier:identifier];

        if (listenerBlock)
        {
          listenerBlock(info);
        }
    }
}

- (id)listenerBlockForIdentifier:(NSString *)identifier
{
    return [self.listenerBlocks valueForKey:identifier];
}

- (void)sendNotificationForMessageWithIdentifier:(NSString *)identifier {
    CFNotificationCenterRef const center = CFNotificationCenterGetDarwinNotifyCenter();
    CFDictionaryRef const userInfo = NULL;
    BOOL const deliverImmediately = YES;
    CFStringRef str = (__bridge CFStringRef)identifier;
    CFNotificationCenterPostNotification(center, str, NULL, userInfo, deliverImmediately);
}

- (void)registerForNotificationsWithIdentifier:(NSString *)identifier
{
    CFNotificationCenterRef const center = CFNotificationCenterGetDarwinNotifyCenter();
    CFStringRef str = (__bridge CFStringRef)identifier;
    CFNotificationCenterAddObserver(center, (__bridge const void *)(self), _panoNotificationCallback, str, NULL, CFNotificationSuspensionBehaviorDeliverImmediately);
}

void _panoNotificationCallback (CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef userInfo)
{
    NSString *identifier = (__bridge NSString *)name;
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:identifier forKey:kPanoNotificationIdentifier];
    if (userInfo != NULL) {
        [info setObject:(__bridge id _Nonnull)(userInfo) forKey:kPanoNotificationIdentifierUserInfo];
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:kPanoNotificationName object:nil userInfo:info];
}
@end
