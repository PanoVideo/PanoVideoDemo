//
//  PanoUserListViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserListViewController.h"
#import "PanoBaseMediaView.h"
#import "PanoArrayDataSource.h"
#import "PanoUserCell.h"
#import "PanoAvatorUtil.h"
#import "PanoCallClient.h"
#import "PanoDefine.h"
#import "UIImage+Extension.h"

@interface PanoUserListViewController ()<UITableViewDelegate,
                                         UITableViewDataSource,
                                         PanoUserDelegate,
                                         UISearchBarDelegate>
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, copy) NSArray *dataSource;
@property (nonatomic, strong) UIView *footerView;
@property (nonatomic, strong) UIButton *presenterBtn;
@property (nonatomic, copy) NSString *searchText;

@end

@implementation PanoUserListViewController

- (void)initViews {
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.rowHeight = 55;
    _tableView.tableFooterView = [[UIView alloc] init];
    _tableView.showsVerticalScrollIndicator = false;
    _tableView.backgroundColor = [UIColor pano_colorWithHexString:@"#F5F7FF"];
    [_tableView registerClass:[PanoUserCell class] forCellReuseIdentifier:@"CellID"];
    self.view.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:_tableView];
    [self initFooterView];
    [self initHeaderView];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"btn.back"] style:0 target:self action:@selector(dismiss)];
    //self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"分享", nil) style:0 target:self action:@selector(shareAction)];
}

- (void)initConstraints {
    [_tableView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(self.view).insets(UIEdgeInsetsMake(0, 0, 88, 0));
    }];
    [_footerView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.bottom.mas_equalTo(self.view);
        make.height.mas_equalTo(88);
    }];
    [_presenterBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.mas_equalTo(0);
        make.width.mas_equalTo(200);
        make.height.mas_equalTo(44);
    }];
    [_tableView.tableHeaderView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(self.view);
    }];
    [self.tableView.tableHeaderView layoutIfNeeded];
}

- (void)initService {
    [PanoCallClient.shared.userMgr addDelegate:self];
    [self reloadViews];
    [self updatePresenterBtn];
}

- (void)reloadViews {
    self.dataSource = [[PanoCallClient.shared.userMgr allUsers] sortedArrayUsingComparator:^NSComparisonResult(PanoUserInfo *_Nonnull obj1, PanoUserInfo *_Nonnull obj2) {
        if (obj1.userId == PanoCallClient.shared.userId) {
            return NSOrderedAscending;
        }
        if (obj2.userId == PanoCallClient.shared.userMgr.hostId) {
            return NSOrderedDescending;
        }
        return NSOrderedSame;
    }];
    if (self.searchText && self.searchText.length > 0) {
        // https://www.coder.work/article/10468 NSPredicate 对多关系不区分大小写匹配
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"userName CONTAINS[c] %@", self.searchText];
        self.dataSource = [self.dataSource filteredArrayUsingPredicate:predicate];
    }
    [self.tableView reloadData];
    self.title = [NSString stringWithFormat:@"%@(%lu)",NSLocalizedString(@"用户列表", nil),(unsigned long)self.dataSource.count];
    [self updatePresenterBtn];
}

- (void)initFooterView {
    UIButton * btn = [UIButton buttonWithType:UIButtonTypeCustom];
    [btn setTitle:NSLocalizedString(@"成为主持人", nil) forState:UIControlStateNormal];
    btn.layer.cornerRadius = 4;
    btn.layer.masksToBounds = true;
    UIImage *normalImage = [UIImage pano_imageWithColor:appHighlightedColor() size:CGSizeMake(200, 44)];
    UIImage *disabledImage = [UIImage pano_imageWithColor:[UIColor pano_colorWithHexString:@"#CCCCCC"] size:CGSizeMake(200, 44)];
    [btn setBackgroundImage:normalImage forState:UIControlStateNormal];
    [btn setBackgroundImage:disabledImage forState:UIControlStateDisabled];
    [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
    [btn addTarget:self action:@selector(applyBecomePresenter) forControlEvents:UIControlEventTouchUpInside];
    _presenterBtn = btn;
    _footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 80)];
    _footerView.backgroundColor = _tableView.backgroundColor;
    [_footerView addSubview:btn];
    [self.view addSubview:_footerView];
}

- (void)initHeaderView {
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectZero];
    UISearchBar *searchBar = [[UISearchBar alloc] initWithFrame:CGRectZero];
    searchBar.delegate = self;
    [headerView addSubview:searchBar];
    [searchBar mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.mas_equalTo(0);
    }];
    _tableView.tableHeaderView = headerView;
}

- (void)applyBecomePresenter {
    [PanoCallClient.shared.userMgr setMeetingHostId:PanoCallClient.shared.userId];
}

- (void)updatePresenterBtn {
    _presenterBtn.enabled = PanoCallClient.shared.userMgr.hostId == 0;
}

#pragma mark -- PanoUserDelegate
- (void)onUserListChanged {
    [self reloadViews];
}

- (void)onPresenterDidChanged {
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
    [cell.iconButton setBackgroundImage:[UIImage pano_imageWithColor:[PanoAvatorUtil getRandomColor:user.userName] size:CGSizeMake(44, 44)] forState:UIControlStateNormal];
    cell.titleLabel.text = user.userName;
    cell.rightLabel.text = user.role == PanoUserRole_Host ? NSLocalizedString(@"主持人", nil) : @"";
    cell.audioImageView.image = user.userListAudioImage;
    cell.videoImageView.image = user.userListVideoImage;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:true];
    [self.view endEditing:true];
    if (indexPath.row >= self.dataSource.count) {
        return;
    }
    PanoUserInfo *user = self.dataSource[indexPath.row];
    UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
    if (user.role == PanoUserRole_None && PanoCallClient.shared.userMgr.me.role == PanoUserRole_Host) {
        UIAlertController * alert = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle: UIAlertControllerStyleActionSheet];
        UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:nil];
        [alert addAction:cancel];
        UIAlertAction *okAction = [UIAlertAction actionWithTitle:[NSString stringWithFormat:NSLocalizedString(@"设为主持人", nil), user.userName] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self showSetHostAlert:user];
        }];
        [alert addAction:okAction];
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
            alert.popoverPresentationController.sourceView = cell;
            alert.popoverPresentationController.sourceRect = cell.bounds;
        }
        [self presentViewController:alert animated:YES completion:nil];
    }
}

- (void)showSetHostAlert:(PanoUserInfo *)user {
    UIAlertController * alert = [UIAlertController alertControllerWithTitle:[NSString stringWithFormat:NSLocalizedString(@"确认将%@设置为主持人？", nil), user.userName] message:nil preferredStyle: UIAlertControllerStyleAlert];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"确认", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [PanoCallClient.shared.userMgr setMeetingHostId:user.userId];
    }];
    [alert addAction:okAction];
    UIAlertAction * cancel = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:nil];
    [alert addAction:cancel];
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)shareAction {
    NSString *title = [NSString stringWithFormat:@"https://%@/m/open.html?roomId=%@",PanoApplinks,PanoCallClient.shared.roomId];
    [self shareWithTitle:title];
}

- (void)shareWithTitle:(NSString *)title {
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:@[[NSURL URLWithString:title]] applicationActivities:nil];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        activityVC.popoverPresentationController.barButtonItem = self.navigationItem.rightBarButtonItem;
    }
    [self presentViewController:activityVC animated:YES completion:nil];
}

#pragma mark -- UISearchBarDelegate
- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText {
    NSLog(@"searchText: %@",searchText);
    self.searchText = searchText;
    [self reloadViews];
}

@end
