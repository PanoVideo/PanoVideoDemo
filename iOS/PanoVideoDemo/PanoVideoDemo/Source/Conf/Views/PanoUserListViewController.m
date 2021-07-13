//
//  PanoUserListViewController.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserListViewController.h"
#import "PanoBaseMediaView.h"
#import "PanoServiceManager.h"
#import "PanoUserService.h"
#import "PanoArrayDataSource.h"
#import "PanoUserCell.h"
#import "PanoAvatorUtil.h"
#import "PanoCallClient.h"
#import "PanoDefine.h"

@interface PanoUserListViewController ()<UITableViewDelegate,UITableViewDataSource, PanoUserDelegate>
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, copy) NSArray *dataSource;

@end

@implementation PanoUserListViewController

- (void)initViews {
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.rowHeight = 55;
    _tableView.tableFooterView = [[UIView alloc] init];
    _tableView.showsVerticalScrollIndicator = false;
    _tableView.allowsSelection = false;
//    _tableView.backgroundColor = [UIColor pano_colorWithHexString:@"f1f1f1"];
    [_tableView registerClass:[PanoUserCell class] forCellReuseIdentifier:@"CellID"];
    [self.view addSubview:_tableView];
    
    self.view.backgroundColor = [UIColor whiteColor];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"btn.back"] style:0 target:self action:@selector(dismiss)];
    //self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"分享", nil) style:0 target:self action:@selector(shareAction)];
}

- (void)initConstraints {
    [_tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.view);
    }];
}

- (void)initService {
    PanoUserService *userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
    [userService addDelegate:self];
    [self reloadViews];
}

- (void)reloadViews {
    PanoUserService *userService = [PanoServiceManager serviceWithType:PanoUserServiceType];
    self.dataSource = [userService allUsers];
    [self.tableView reloadData];
    self.title = [NSString stringWithFormat:@"%@(%lu)",NSLocalizedString(@"用户列表", nil),(unsigned long)self.dataSource.count];
}

#pragma mark -- PanoUserDelegate
- (void)onUserStatusChanged:(PanoUserInfo *)user {
    [self reloadViews];
}

- (void)onUserRemoved:(PanoUserInfo *)user {
    [self reloadViews];
}

- (void)onUserAdded:(PanoUserInfo *)user {
    [self reloadViews];
}

#pragma mark --
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    PanoUserCell *cell = [tableView dequeueReusableCellWithIdentifier:@"CellID" forIndexPath:indexPath];
    PanoUserInfo *user = self.dataSource[indexPath.row];
    [cell.iconButton setTitle:[PanoAvatorUtil getAvatarName:user.userName] forState:UIControlStateNormal];
    [cell.iconButton setBackgroundColor:[PanoAvatorUtil getRandomColor:user.userName]];
    cell.titleLabel.text = user.userName;
    cell.audioImageView.highlighted = user.audioStatus == PanoUserAudio_Unmute;
    cell.videoImageView.highlighted = user.videoStaus == PanoUserVideo_Unmute;
    return cell;
}

- (void)dismiss {
    [self dismissViewControllerAnimated:true completion:^{
    }];
}

- (void)shareAction {
    
    NSString *title = [NSString stringWithFormat:@"https://%@/m/open.html?roomId=%@",PanoApplinks,PanoCallClient.sharedInstance.roomId];
    [self shareWithTitle:title];
}

- (void)shareWithTitle:(NSString *)title {
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:@[[NSURL URLWithString:title]] applicationActivities:nil];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        activityVC.popoverPresentationController.barButtonItem = self.navigationItem.rightBarButtonItem;
        [self presentViewController:activityVC animated:YES completion:nil];
    }else{
        [self presentViewController:activityVC animated:YES completion:nil];
    }
}

@end
