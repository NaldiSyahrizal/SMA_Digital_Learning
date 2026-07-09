from PIL import Image

def remove_white_bg(input_path, output_path):
    img = Image.open(input_path).convert("RGBA")
    datas = img.getdata()
    
    newData = []
    for item in datas:
        # If the pixel is close to white, make it transparent
        # item is (R, G, B, A)
        if item[0] > 220 and item[1] > 220 and item[2] > 220:
            # Full transparency
            newData.append((255, 255, 255, 0))
        else:
            newData.append(item)
            
    img.putdata(newData)
    img.save(output_path, "PNG")

remove_white_bg('app/src/main/res/drawable/logo_lms.png', 'app/src/main/res/drawable/logo_lms_transparent.png')
print('Background removed.')
