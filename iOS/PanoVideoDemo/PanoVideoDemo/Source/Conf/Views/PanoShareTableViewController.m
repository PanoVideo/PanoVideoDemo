//
//  PanoShareTableViewController.m
//  PanoVideoDemo
//
//  
//  Copyright © 2022 Pano. All rights reserved.
//

#import "PanoShareTableViewController.h"
#import "UIColor+Extension.h"
#import "PanoCallClient.h"

@interface PanoShareTableViewController ()
@property (nonatomic, strong) NSArray *dataSource;
@property (nonatomic, assign) NSUInteger selectedIndex;
@end

@implementation PanoShareTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"cellID"];
    self.title = NSLocalizedString(@"共享屏幕设置", nil);
    self.tableView.tableFooterView = [UIView new];
    _selectedIndex = PanoCallClient.shared.config.shareOption;
    self.dataSource = PanoCallClient.shared.config.shareOptions;
}
- (IBAction)dismiss:(id)sender {
    if (self.selectedIndex != PanoCallClient.shared.config.shareOption) {
        PanoCallClient.shared.config.shareOption = self.selectedIndex;
        [PanoCallClient.shared.rtcService setProperty:@{@"share" : @(self.selectedIndex)} forKey:PanoSettingKey];
    }
    [self dismissViewControllerAnimated:true completion:nil];
}


#pragma mark - Table view data source
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cellID" forIndexPath:indexPath];
    cell.textLabel.text = self.dataSource[indexPath.row];
    cell.textLabel.textColor = [UIColor pano_colorWithHexString:@"#333333"];
    cell.accessoryType = _selectedIndex == indexPath.row ?  UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:true];
    _selectedIndex = indexPath.row;
    [self.tableView reloadData];
}

@end
