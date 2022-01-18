//
//  PanoUserCell.m
//  PanoVideoDemo
//
//  
//  Copyright © 2020 Pano. All rights reserved.
//

#import "PanoUserCell.h"
#import "Masonry.h"
#import "PanoAvatorUtil.h"

@implementation PanoUserCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        
        _iconButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [self.iconButton setTitle:@"" forState:UIControlStateNormal];
        [self.iconButton setTitle:@"" forState:UIControlStateHighlighted];
        [self.iconButton setTitleColor: [UIColor whiteColor] forState:UIControlStateNormal];
        [self.iconButton setTitleColor: [UIColor whiteColor] forState:UIControlStateHighlighted];
        [self.iconButton.titleLabel setFont:[UIFont fontWithName:@"Helvetica-Bold" size:15]];
        [self.iconButton setClipsToBounds:YES];
//        [self.iconButton setEnabled:NO];
        self.iconButton.layer.cornerRadius = 3;
        [self.contentView addSubview:self.iconButton];
        
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.font = [UIFont systemFontOfSize:14];
        _titleLabel.text = @"";
        _titleLabel.textAlignment = NSTextAlignmentLeft;
        [self.contentView addSubview:_titleLabel];
        
        _rightLabel = [[UILabel alloc] init];
        _rightLabel.font = [UIFont systemFontOfSize:14];
        _rightLabel.text = @"";
        _rightLabel.textAlignment = NSTextAlignmentRight;
        [self.contentView addSubview:_rightLabel];
        
        _audioImageView = [[UIImageView alloc] init];
        _audioImageView.contentMode = UIViewContentModeScaleAspectFit;
        [self.contentView addSubview:_audioImageView];
        
        _videoImageView = [[UIImageView alloc] init];
        _videoImageView.contentMode = UIViewContentModeScaleAspectFit;
        [self.contentView addSubview:_videoImageView];
        
        [_iconButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.mas_equalTo(self.contentView);
            make.left.mas_equalTo(self.contentView).mas_offset(15);
            make.size.mas_equalTo(CGSizeMake(30, 30));
        }];
        
        [_titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.mas_equalTo(self.contentView);
            make.left.mas_equalTo(self.iconButton.mas_right).mas_offset(15);
            make.right.mas_equalTo(self.rightLabel.mas_left).mas_offset(-15);
        }];
        
        [_rightLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.mas_equalTo(self.contentView);
            make.right.mas_equalTo(self.audioImageView.mas_left).mas_offset(-15);
        }];
        
        [_audioImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.mas_equalTo(self.contentView);
            make.right.mas_equalTo(self.videoImageView.mas_left).mas_offset(-15);
        }];
        
        [_videoImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerY.mas_equalTo(self.contentView);
            make.right.mas_equalTo(self.contentView).mas_offset(-15);
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
