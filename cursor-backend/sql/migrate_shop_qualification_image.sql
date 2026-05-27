-- 修改 shop_qualification 表的图片字段为 TEXT 类型以支持更大的 Base64 数据
ALTER TABLE shop_qualification MODIFY COLUMN business_license TEXT;
ALTER TABLE shop_qualification MODIFY COLUMN id_card_front TEXT;
ALTER TABLE shop_qualification MODIFY COLUMN id_card_back TEXT;
ALTER TABLE shop_qualification MODIFY COLUMN quality_cert TEXT;
