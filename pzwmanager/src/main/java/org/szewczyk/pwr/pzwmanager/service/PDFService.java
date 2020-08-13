package org.szewczyk.pwr.pzwmanager.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Order;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.BorderStyle;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

@Service
public class PDFService {
    public void createOrderConfirmation(Order order) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page1 = new PDPage(PDRectangle.A4);
        document.addPage(page1);
        PDPageContentStream contentStream = new PDPageContentStream(document, page1);
        String srcPath = "./src/main/resources/static/";
        PDImageXObject logo1 = PDImageXObject.createFromFile(srcPath + "img/1.jpg", document);
        PDType0Font font = PDType0Font.load(document, new File(srcPath + "fonts/SourceSans/SourceSansPro-Light.ttf"));

        contentStream.drawImage(logo1, 450, 645, 75, 75);
        contentStream.beginText();

        contentStream.setFont(font, 12);
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(25, 700);
//        Header
        contentStream.showText("Polski Związek Wędkarski");
        contentStream.newLine();
        contentStream.showText("Okręg w Wałbrzychu");
        contentStream.newLine();
        contentStream.showText("Koło nr 13 im. Władysława Turonia");
        contentStream.newLine();
        contentStream.showText("ul. Główna 67");
        contentStream.newLine();
        contentStream.showText("57-350 Kudowa Zdrój");
        contentStream.endText();

        contentStream.moveTo(100, 550);
        contentStream.lineTo(500, 550);
        contentStream.stroke();
        addCenteredText("Potwierdzenie płatności", font, 24, contentStream, page1, new Point2D.Float(0, 100));
        addCenteredText("nr " + order.getOrderNumber(), font, 18, contentStream, page1, new Point2D.Float(0, 75));
        contentStream.moveTo(100, 480);
        contentStream.lineTo(500, 480);
        contentStream.stroke();

        Table myTable = Table.builder()
                .addColumnsOfWidth(470, 70)
                .padding(8)
                .addRow(Row.builder()
                        .add(TextCell.builder().text("Typ pozwolenia").build())
                        .add(TextCell.builder().text("Koszt").build())
                        .font(font)
                        .fontSize(12)
                        .backgroundColor(new Color(240,240,240))
                        .borderStyle(BorderStyle.SOLID)
                        .borderColor(new Color(150,150,150))
                        .borderWidth(0.5f)
                        .build())
                .addRow(Row.builder()
                        .add(TextCell.builder().text("Test 123").build())
                        .add(TextCell.builder().text("25.00").build())
                        .font(font)
                        .fontSize(12)
                        .borderStyle(BorderStyle.SOLID)
                        .borderColor(new Color(150,150,150))
                        .borderWidth(0.5f)
                        .build())
                .build();
        TableDrawer tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(25)
                .startY(420)
                .table(myTable)
                .build();

        tableDrawer.draw();

        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(25, 350);
//        contentStream.showText("Nr zamówienia: " + order.getOrderNumber());
//        contentStream.newLine();
        contentStream.showText("ID PayU: " + order.getPayuOrderId());

        contentStream.endText();
        contentStream.close();


        document.save(new File("C:" + File.separator + "testy" + File.separator + "test_doc.pdf"));
        System.out.println("PDF saved");
        document.close();
    }

    private void addCenteredText(String text, PDFont font, int fontSize, PDPageContentStream stream, PDPage page, Point2D.Float offset) throws IOException{
        stream.setFont(font, fontSize);
        Point2D.Float pageCenter = getCenter(page);
        float stringWidth = font.getStringWidth(text) / 1000 * fontSize;

        stream.beginText();
        float textX = pageCenter.x - stringWidth / 2F + offset.x;
        float textY = pageCenter.y + offset.y;
        stream.setTextMatrix(Matrix.getTranslateInstance(textX, textY));
        stream.showText(text);
        stream.endText();
    }

    private Point2D.Float getCenter(PDPage page){
        return new Point2D.Float(page.getMediaBox().getWidth() / 2F, page.getMediaBox().getHeight() / 2F);
    }
}
