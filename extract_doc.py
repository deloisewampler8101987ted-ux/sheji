import re
path = r'c:\Users\lenovo\.trae-cn\attachments\6a447dc724fbb3aa3ac5ddc0\f9d2e449-4320-41a2-b5f7-89a4510d5c17_航空订票系统-TRAE编程任务书.docx'
with open(path, 'rb') as f:
    data = f.read()

# Decode with different encodings
for enc in ['utf-8', 'gbk', 'gb2312', 'gb18030', 'utf-16-le', 'utf-16-be']:
    try:
        text = data.decode(enc, errors='ignore')
        # Extract Chinese text
        chinese_chars = re.findall(r'[\u4e00-\u9fff]+', text)
        if chinese_chars:
            print(f"=== Encoding: {enc} ===")
            for c in chinese_chars:
                if len(c) >= 2:
                    print(c, end=' ')
            print()
    except:
        pass