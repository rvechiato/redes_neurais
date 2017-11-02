package br.com.unip.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class Entry extends JPanel {

	
	protected Image entryImage;

	
	protected Graphics entryGraphics;

	
	protected int lastX = -1;

	
	protected int lastY = -1;

	
	protected Sample sample;

	protected int downSampleLeft;

	protected int downSampleRight;

	
	protected int downSampleTop;

	protected int downSampleBottom;

	
	protected double ratioX;

	protected double ratioY;

	protected int pixelMap[];


	Entry() {
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
	}


	protected void initImage() {
		entryImage = createImage(getWidth(), getHeight());
		entryGraphics = entryImage.getGraphics();
		entryGraphics.setColor(Color.white);
		entryGraphics.fillRect(0, 0, getWidth(), getHeight());
	}


	public void paint(Graphics g) {
		if (entryImage == null)
			initImage();
		g.drawImage(entryImage, 0, 0, this);
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.red);
		g.drawRect(downSampleLeft, downSampleTop, downSampleRight - downSampleLeft, downSampleBottom - downSampleTop);

	}


	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_PRESSED)
			return;
		lastX = e.getX();
		lastY = e.getY();
	}


	protected void processMouseMotionEvent(MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_DRAGGED)
			return;

		entryGraphics.setColor(Color.black);
		entryGraphics.drawLine(lastX, lastY, e.getX(), e.getY());
		getGraphics().drawImage(entryImage, 0, 0, this);
		lastX = e.getX();
		lastY = e.getY();
	}


	public void setSample(Sample s) {
		sample = s;
	}


	public Sample getSample() {
		return sample;
	}


	protected boolean hLineClear(int y) {
		int w = entryImage.getWidth(this);
		for (int i = 0; i < w; i++) {
			if (pixelMap[(y * w) + i] != -1)
				return false;
		}
		return true;
	}


	protected boolean vLineClear(int x) {
		int w = entryImage.getWidth(this);
		int h = entryImage.getHeight(this);
		for (int i = 0; i < h; i++) {
			if (pixelMap[(i * w) + x] != -1)
				return false;
		}
		return true;
	}


	protected void findBounds(int w, int h) {
		// top line
		for (int y = 0; y < h; y++) {
			if (!hLineClear(y)) {
				downSampleTop = y;
				break;
			}

		}
		// bottom line
		for (int y = h - 1; y >= 0; y--) {
			if (!hLineClear(y)) {
				downSampleBottom = y;
				break;
			}
		}
		// left line
		for (int x = 0; x < w; x++) {
			if (!vLineClear(x)) {
				downSampleLeft = x;
				break;
			}
		}

		// right line
		for (int x = w - 1; x >= 0; x--) {
			if (!vLineClear(x)) {
				downSampleRight = x;
				break;
			}
		}
	}


	protected boolean downSampleQuadrant(int x, int y) {
		int w = entryImage.getWidth(this);
		int startX = (int) (downSampleLeft + (x * ratioX));
		int startY = (int) (downSampleTop + (y * ratioY));
		int endX = (int) (startX + ratioX);
		int endY = (int) (startY + ratioY);

		for (int yy = startY; yy <= endY; yy++) {
			for (int xx = startX; xx <= endX; xx++) {
				int loc = xx + (yy * w);

				if (pixelMap[loc] != -1)
					return true;
			}
		}

		return false;
	}

	public void downSample() {
		int w = entryImage.getWidth(this);
		int h = entryImage.getHeight(this);

		PixelGrabber grabber = new PixelGrabber(entryImage, 0, 0, w, h, true);
		try {

			grabber.grabPixels();
			pixelMap = (int[]) grabber.getPixels();
			findBounds(w, h);

			// now downsample
			SampleData data = sample.getData();

			ratioX = (double) (downSampleRight - downSampleLeft) / (double) data.getWidth();
			ratioY = (double) (downSampleBottom - downSampleTop) / (double) data.getHeight();

			for (int y = 0; y < data.getHeight(); y++) {
				for (int x = 0; x < data.getWidth(); x++) {
					if (downSampleQuadrant(x, y))
						data.setData(x, y, true);
					else
						data.setData(x, y, false);
				}
			}

			sample.repaint();
			repaint();
		} catch (InterruptedException e) {
		}
	}


	public void clear() {
		this.entryGraphics.setColor(Color.white);
		this.entryGraphics.fillRect(0, 0, getWidth(), getHeight());
		this.downSampleBottom = this.downSampleTop = this.downSampleLeft = this.downSampleRight = 0;
		repaint();
	}
}