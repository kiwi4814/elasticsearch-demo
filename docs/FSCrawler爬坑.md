[Github地址](https://github.com/dadoonet/fscrawler)

[官方文档地址](https://fscrawler.readthedocs.io/en/latest/)

## 简要介绍

File System Crawler for Elasticsearch是基于Es的一个文档导入插件，只需要简单的配置就可以实现将本地文件系统的文件导入到ES中进行检索，同时支持丰富的文件格式（txt.pdf,html,word…）等等。

这里所用的版本为Elasticsearch Rest Client 7.3.1和FS Crawler 2.7-SNAPSHOT，系统为Windows 10 Version 1903。

启动

```shell
bin/fscrawler job_name
```

`job_name`是你要为此导入任务起的名字

启动参数

```powershell
# 启动运行一次后结束
bin/fscrawler job_name --loop 1

```

首先会去读取默认配置文件的地址：`C:\Users\{user}\.fscrawler\{job_name}\_settings.yaml`

如果不存在，会确认是否创建，创建完成后简单的配置如下：

```yaml
---
# 任务名称
name: "job_name"
# File System相关设置
fs:
  # 需要导入的文件夹地址
  url: "\\tmp\\es"
  # 更新频率，默认15min
  update_rate: "15m"
  # 需要移除的文件规则
  excludes:
  - "*/~*"
  # 支持json
  json_support: false
  filename_as_id: false
  add_filesize: true
  remove_deleted: true
  add_as_inner_object: false
  store_source: false
  index_content: true
  attributes_support: false
  raw_metadata: false
  xml_support: false
  index_folders: true
  lang_detect: false
  continue_on_error: false
  ocr:
    language: "eng+chi_sim+chi_tra"
    enabled: true
    pdf_strategy: "ocr_and_text"
  follow_symlinks: false
elasticsearch:
  nodes:
  - url: "http://127.0.0.1:9200"
  bulk_size: 100
  flush_interval: "5s"
  byte_size: "10mb"

```

`fs.url`为要导入的文件夹地址，windows下可改为：`C:\\tmp\\es`

`elasticsearch`下设置`url`

配置忽略文件`Ignoring folders`

## OCR集成（OCR integration）

| Name                  | Default value  | Documentation                                                |
| --------------------- | -------------- | ------------------------------------------------------------ |
| `fs.ocr.enabled`      | `true`         | [Disable/Enable OCR](https://fscrawler.readthedocs.io/en/latest/user/ocr.html#disable-enable-ocr) |
| `fs.ocr.language`     | `"eng"`        | [OCR Language](https://fscrawler.readthedocs.io/en/latest/user/ocr.html#ocr-language) |
| `fs.ocr.path`         | `null`         | [OCR Path](https://fscrawler.readthedocs.io/en/latest/user/ocr.html#ocr-path) |
| `fs.ocr.data_path`    | `null`         | [OCR Data Path](https://fscrawler.readthedocs.io/en/latest/user/ocr.html#ocr-data-path) |
| `fs.ocr.output_type`  | `txt`          | [OCR Output Type](https://fscrawler.readthedocs.io/en/latest/user/ocr.html#ocr-output-type) |
| `fs.ocr.pdf_strategy` | `ocr_and_text` | [OCR PDF Strategy](https://fscrawler.readthedocs.io/en/latest/user/ocr.html#ocr-pdf-strategy) |

## 使用Restful

https://fscrawler.readthedocs.io/en/latest/user/rest.html



