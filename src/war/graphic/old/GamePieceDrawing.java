package war.graphic;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class GamePieceDrawing extends JPanel {
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // アンチエイリアスを有効化（滑らかな描画）
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 駒の描画例1: 円形の駒（チェッカーのような）
        drawCirclePiece(g2d, 100, 100, Color.RED);
        drawCirclePiece(g2d, 200, 100, Color.BLACK);
        
        // 駒の描画例2: 五角形の駒
        drawPentagonPiece(g2d, 100, 250, Color.BLUE);
        
        // 駒の描画例3: 将棋風の駒
        drawShogiPiece(g2d, 250, 250, "王", Color.ORANGE);
    }
    
    // 円形の駒を描く
    private void drawCirclePiece(Graphics2D g2d, int x, int y, Color color) {
        int size = 60;
        
        // 影
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(x + 3, y + 3, size, size);
        
        // 駒本体
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);
        
        // 縁取り
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, size, size);
    }
    
    // 五角形の駒を描く
    private void drawPentagonPiece(Graphics2D g2d, int x, int y, Color color) {
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];
        int radius = 40;
        
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(i * 72 - 90);
            xPoints[i] = x + (int)(radius * Math.cos(angle));
            yPoints[i] = y + (int)(radius * Math.sin(angle));
        }
        
        Polygon pentagon = new Polygon(xPoints, yPoints, 5);
        
        // 駒本体
        g2d.setColor(color);
        g2d.fillPolygon(pentagon);
        
        // 縁取り
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(pentagon);
    }
    
    // 将棋風の駒を描く
    private void drawShogiPiece(Graphics2D g2d, int x, int y, 
                                String text, Color color) {
        int width = 50;
        int height = 60;
        
        // 五角形（上が尖った形）
        int[] xPoints = {x + width/2, x + width, x + width, x, x};
        int[] yPoints = {y, y + height/3, y + height, y + height, y + height/3};
        Polygon piece = new Polygon(xPoints, yPoints, 5);
        
        // 駒本体
        g2d.setColor(color);
        g2d.fillPolygon(piece);
        
        // 縁取り
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(piece);
        
        // 文字
        g2d.setFont(new Font("Serif", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, 
                      x + (width - textWidth) / 2, 
                      y + height/2 + textHeight/3);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("ゲームの駒");
        GamePieceDrawing panel = new GamePieceDrawing();
        
        frame.add(panel);
        frame.setSize(500, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}