//
//  PanoMenuCell.m
//  PanoVideoDemo
//
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoMenuCell.h"
#import "PanoBaseView.h"

@interface PanoMenuCell ()

@property (nonatomic, strong) UILabel *descLabel;
@property (nonatomic, strong) UIImageView *icon;
@property (nonatomic, strong) UIView *lineView;
@end

@implementation PanoMenuCell


- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
//        self.selectionStyle = UITableViewCellSelectionStyleBlue;
        
        _descLabel = [[UILabel alloc] init];
        _descLabel.font = [UIFont systemFontOfSize:14];
        _descLabel.text = @"";
        _descLabel.textAlignment = NSTextAlignmentLeft;
        [self.contentView addSubview:_descLabel];
        
        _icon = [[UIImageView alloc] init];
        _icon.contentMode = UIViewContentModeScaleAspectFit;
        [_icon sizeToFit];
        [self.contentView addSubview:_icon];
        
        UIView *lineView = [[UIView alloc] init];
        lineView.backgroundColor = [UIColor pano_colorWithHexString:@"f1f1f1"];
        _lineView = lineView;
        [self.contentView addSubview:lineView];
        
        
        [_icon mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.mas_equalTo(self.contentView).mas_offset(DefaultFixSpace);
            make.centerY.mas_equalTo(self.contentView);
            make.size.mas_equalTo(CGSizeMake(24, 24));
        }];
        
        [_descLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.mas_equalTo(self.icon.mas_right).mas_offset(DefaultFixSpace);
            make.centerY.mas_equalTo(self.icon);
        }];
        
        [_lineView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.bottom.right.mas_equalTo(self.contentView).insets(UIEdgeInsetsMake(0, 0, -0.5, 0));
            make.height.mas_equalTo(0.5);
        }];
    }
    return self;
}

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
