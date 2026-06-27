package com.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "验证码管理", description = "图形验证码相关接口")
public class CaptchaController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "返回验证码图片，Captcha-Key通过响应头返回")
    public void getCaptcha(HttpServletResponse response) throws IOException {
        // 生成4位随机验证码
        String code = String.format("%04d", new Random().nextInt(10000));
        String key = UUID.randomUUID().toString();

        // 存入Redis，5分钟过期
        redisTemplate.opsForValue().set("captcha:" + key, code, 5, TimeUnit.MINUTES);

        // 设置响应头（必须在写入body之前设置，否则header不会生效）
        response.setContentType("image/png");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Captcha-Key", key);

        // 生成验证码图片
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景色
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);

        // 画干扰线
        Random random = new Random();
        g.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 20; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 画验证码文字
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String[] chars = code.split("");
        for (int i = 0; i < chars.length; i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            // 随机旋转
            double theta = Math.toRadians(random.nextInt(20) - 10);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.rotate(theta, 20 + i * 25, 28);
            g2.drawString(chars[i], 20 + i * 25, 28);
            g2.dispose();
        }

        g.dispose();

        // 输出图片
        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
        out.flush();
    }
}
