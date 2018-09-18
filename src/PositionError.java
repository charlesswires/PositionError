import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;


public class PositionError extends JPanel{

	private List<Integer> samplesWiFi1 = new ArrayList<Integer>();
	private List<Integer> samplesWiFi2 = new ArrayList<Integer>();
	private List<Integer> samplesWiFi3 = new ArrayList<Integer>();
	private PositionError(String file) throws IOException {
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
		Reader in = new FileReader(file);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			samplesWiFi1.add(Integer.parseInt(record.get(3).trim()));
		}
		for (Integer sample: samplesWiFi1) {
			int i = (int)Math.round(Math.random()*samplesWiFi1.size());
			i = (i >= samplesWiFi1.size())?samplesWiFi1.size()-1:i;
			samplesWiFi2.add(samplesWiFi1.get(i));
		} 
		for (Integer sample: samplesWiFi1) {
			int i = (int)Math.round(Math.random()*samplesWiFi1.size());
			i = (i >= samplesWiFi1.size())?samplesWiFi1.size()-1:i;
			samplesWiFi3.add(samplesWiFi1.get(i));
		} 
	}
	private static double NOISE_VALUE = 2.0;

	protected static boolean render = true;
	JLabel minxl= new JLabel("No Test Pts");
	JTextField minx= new JTextField("2",10);
	JLabel maxxl = new JLabel("Scale(pixels/m)");
	JTextField maxx =new JTextField("5",20);
	JLabel avel = new JLabel("Average No");
	JTextField ave =new JTextField("1",10);	
	JButton doRender= new JButton("Render");
	JPanel canvas = new JPanel();
	JFrame jfrm = new JFrame("Position Error ");
	static PositionError pe;

	class Point{

		double x, y;
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int height = getHeight();
		int width = getWidth();
		int mx = Integer.parseInt(maxx.getText());
		g.setColor(Color.LIGHT_GRAY);
		for(int i = 0; i*mx < height;i++) {
			g.drawLine(0, i*mx, width, i*mx);
		}
		for(int i = 0; i*mx < width;i++) {
			g.drawLine(i*mx, 0, i*mx, height);
		}
		g.setColor(Color.RED);
		g.fillRect(mx, height-2*mx, mx, mx);g.fillRect(mx, mx, mx, mx);g.fillRect(width-2*mx, mx, mx, mx);
		int samples = Integer.parseInt(minx.getText());
		for(int i = 0; i< samples;i++) {
			double x = Math.random()*width;double y = Math.random()*height;
			g.setColor(Color.BLACK);
			List<Point> points = new ArrayList<Point>();
			for (int j = 0; j < samplesWiFi1.size(); j++) {
				Point p = calculateAndDrawLocations(j,x/mx,y/mx,0.0,(height-0.0)/mx,0.0,(double)(height - height)/(double)mx,(double)width/(double)mx,(height -0.0)/mx);
				g.drawLine((int)Math.round(p.x*mx)-mx, height -(int)Math.round(p.y*mx), (int)Math.round(p.x*mx)+mx, height - (int)Math.round(p.y*mx));
				g.drawLine((int)Math.round(p.x*mx), height -(int)Math.round(p.y*mx)-mx, (int)Math.round(p.x*mx), height - (int)Math.round(p.y*mx)+mx);
				points.add(p);
			}
			g.setColor(Color.BLUE);
			g.drawLine((int)Math.round(x-mx), height - (int)Math.round(y), (int)Math.round(x+mx), height -(int)Math.round(y));
			g.drawLine((int)Math.round(x), height - (int)Math.round(y-mx), (int)Math.round(x), height -(int)Math.round(y+mx));
			
			double sumx = 0.0;
			double sumy = 0.0;
			
			for (Point tempPoint:points) {
				sumx += tempPoint.x;
				sumy += tempPoint.y;
				
			}
			double meanx = sumx/points.size();
			double meany = sumy/points.size();
			double sumxx = 0.0;
			double sumyy = 0.0;
			for (Point tempPoint:points) {
				sumxx += (tempPoint.x - meanx)*(tempPoint.x - meanx);
				sumyy += (tempPoint.y - meany)*(tempPoint.y - meany);
				
			}
			g.setColor(Color.BLACK);
			g.fillRect((int)Math.round(x), height-(int)Math.round(y+20), 6*20, 20);
			g.setColor(Color.WHITE);

			g.drawString("sdx="+(double)Math.round(Math.sqrt(sumxx/points.size())*100)/100.0+
					" sdy="+(double)Math.round(Math.sqrt(sumyy/points.size())*100)/100.0, 
					(int)Math.round(x), height-(int)Math.round(y)-3);
			g.setColor(Color.BLUE);
			g.drawLine((int)Math.round(x-2*mx), height - (int)Math.round(y), (int)Math.round(x+2*mx), height -(int)Math.round(y));
			g.drawLine((int)Math.round(x), height - (int)Math.round(y-2*mx), (int)Math.round(x), height -(int)Math.round(y+2*mx));
			
		}
	}


	private Point calculateAndDrawLocations(int i,double x, double y, double x1, double y1, 
			double x2, double y2,
			double x3, double y3) {
		//		I fed the equations into Mathematica, and it gave me this answer.  For
		//		brevity of notation, I changed the equations to
		//
		//		  (x-a)^2 + (y-b)^2 == R
		//		  
		//		  (x-c)^2 + (y-d)^2 == r.
		//
		//		These are the two points:  
		//
		//		{{x -> (a^4 + a^2*b^2 - 2*a^3*c - b^2*c^2 + 2*a*c^3 - c^4 - 2*a^2*b*d + 
		//		       2*b*c^2*d + a^2*d^2 - c^2*d^2 + a^2*r - 2*a*c*r + c^2*r - a^2*R + 
		//		       2*a*c*R - c^2*R + b*((a - c)^2*
		//		           (-a^4 - 2*a^2*b^2 - b^4 + 4*a^3*c + 4*a*b^2*c - 6*a^2*c^2 - 
		//		             2*b^2*c^2 + 4*a*c^3 - c^4 + 4*a^2*b*d + 4*b^3*d - 
		//		             8*a*b*c*d + 4*b*c^2*d - 2*a^2*d^2 - 6*b^2*d^2 + 4*a*c*d^2 - 
		//		             2*c^2*d^2 + 4*b*d^3 - d^4 + 2*a^2*r + 2*b^2*r - 4*a*c*r + 
		//		             2*c^2*r - 4*b*d*r + 2*d^2*r - r^2 + 2*a^2*R + 2*b^2*R - 
		//		             4*a*c*R + 2*c^2*R - 4*b*d*R + 2*d^2*R + 2*r*R - R^2))^(1/2) - 
		//		       d*((a - c)^2*(-a^4 - 2*a^2*b^2 - b^4 + 4*a^3*c + 4*a*b^2*c - 
		//		             6*a^2*c^2 - 2*b^2*c^2 + 4*a*c^3 - c^4 + 4*a^2*b*d + 
		//		             4*b^3*d - 8*a*b*c*d + 4*b*c^2*d - 2*a^2*d^2 - 6*b^2*d^2 + 
		//		             4*a*c*d^2 - 2*c^2*d^2 + 4*b*d^3 - d^4 + 2*a^2*r + 2*b^2*r - 
		//		             4*a*c*r + 2*c^2*r - 4*b*d*r + 2*d^2*r - r^2 + 2*a^2*R + 
		//		             2*b^2*R - 4*a*c*R + 2*c^2*R - 4*b*d*R + 2*d^2*R + 2*r*R - R^2
		//		             ))^(1/2))/(2*(a - c)*(a^2 + b^2 - 2*a*c + c^2 - 2*b*d + d^2)), 
		//		   y -> (a^2*b + b^3 - 2*a*b*c + b*c^2 + a^2*d - b^2*d - 2*a*c*d + c^2*d - 
		//		       b*d^2 + d^3 + b*r - d*r - b*R + d*R - 
		//		       ((a - c)^2*(-a^4 - 2*a^2*b^2 - b^4 + 4*a^3*c + 4*a*b^2*c - 
		//		            6*a^2*c^2 - 2*b^2*c^2 + 4*a*c^3 - c^4 + 4*a^2*b*d + 4*b^3*d - 
		//		            8*a*b*c*d + 4*b*c^2*d - 2*a^2*d^2 - 6*b^2*d^2 + 4*a*c*d^2 - 
		//		            2*c^2*d^2 + 4*b*d^3 - d^4 + 2*a^2*r + 2*b^2*r - 4*a*c*r + 
		//		            2*c^2*r - 4*b*d*r + 2*d^2*r - r^2 + 2*a^2*R + 2*b^2*R - 
		//		            4*a*c*R + 2*c^2*R - 4*b*d*R + 2*d^2*R + 2*r*R - R^2))^(1/2))/
		//		     (2*(a^2 + b^2 - 2*a*c + c^2 - 2*b*d + d^2))}, 
		//
		//
		//		  {x -> (a^4 + a^2*b^2 - 2*a^3*c - b^2*c^2 + 2*a*c^3 - c^4 - 2*a^2*b*d + 
		//		       2*b*c^2*d + a^2*d^2 - c^2*d^2 + a^2*r - 2*a*c*r + c^2*r - a^2*R + 
		//		       2*a*c*R - c^2*R - b*((a - c)^2*
		//		           (-a^4 - 2*a^2*b^2 - b^4 + 4*a^3*c + 4*a*b^2*c - 6*a^2*c^2 - 
		//		             2*b^2*c^2 + 4*a*c^3 - c^4 + 4*a^2*b*d + 4*b^3*d - 
		//		             8*a*b*c*d + 4*b*c^2*d - 2*a^2*d^2 - 6*b^2*d^2 + 4*a*c*d^2 - 
		//		             2*c^2*d^2 + 4*b*d^3 - d^4 + 2*a^2*r + 2*b^2*r - 4*a*c*r + 
		//		             2*c^2*r - 4*b*d*r + 2*d^2*r - r^2 + 2*a^2*R + 2*b^2*R - 
		//		             4*a*c*R + 2*c^2*R - 4*b*d*R + 2*d^2*R + 2*r*R - R^2))^(1/2) + 
		//		       d*((a - c)^2*(-a^4 - 2*a^2*b^2 - b^4 + 4*a^3*c + 4*a*b^2*c - 
		//		             6*a^2*c^2 - 2*b^2*c^2 + 4*a*c^3 - c^4 + 4*a^2*b*d + 4*b^3*d - 
		//		             8*a*b*c*d + 4*b*c^2*d - 2*a^2*d^2 - 6*b^2*d^2 + 4*a*c*d^2 - 
		//		             2*c^2*d^2 + 4*b*d^3 - d^4 + 2*a^2*r + 2*b^2*r - 4*a*c*r + 
		//		             2*c^2*r - 4*b*d*r + 2*d^2*r - r^2 + 2*a^2*R + 2*b^2*R - 
		//		             4*a*c*R + 2*c^2*R - 4*b*d*R + 2*d^2*R + 2*r*R - R^2))^(1/2))/
		//		     (2*(a - c)*(a^2 + b^2 - 2*a*c + c^2 - 2*b*d + d^2)), 
		//		   y -> (a^2*b + b^3 - 2*a*b*c + b*c^2 + a^2*d - b^2*d - 2*a*c*d + c^2*d - 
		//		       b*d^2 + d^3 + b*r - d*r - b*R + d*R + 
		//		       ((a - c)^2*(-a^4 - 2*a^2*b^2 - b^4 + 4*a^3*c + 4*a*b^2*c - 
		//		            6*a^2*c^2 - 2*b^2*c^2 + 4*a*c^3 - c^4 + 4*a^2*b*d + 4*b^3*d - 
		//		            8*a*b*c*d + 4*b*c^2*d - 2*a^2*d^2 - 6*b^2*d^2 + 4*a*c*d^2 - 
		//		            2*c^2*d^2 + 4*b*d^3 - d^4 + 2*a^2*r + 2*b^2*r - 4*a*c*r + 
		//		            2*c^2*r - 4*b*d*r + 2*d^2*r - r^2 + 2*a^2*R + 2*b^2*R - 
		//		            4*a*c*R + 2*c^2*R - 4*b*d*R + 2*d^2*R + 2*r*R - R^2))^(1/2))/
		//		     (2*(a^2 + b^2 - 2*a*c + c^2 - 2*b*d + d^2))}}
		double sum = 0.0;
		for(Integer sample: samplesWiFi1) {
			sum += Math.abs(sample);
		}
		double meanPower = sum/samplesWiFi1.size();
		//		  (x-a)^2 + (y-b)^2 == R
		//		  
		//		  (x-c)^2 + (y-d)^2 == r.
		double r1 = Math.sqrt((x-x1)*(x-x1)+(y-y1)*(y-y1));
		double r2 = Math.sqrt((x-x2)*(x-x2)+(y-y2)*(y-y2));
		double r3 = Math.sqrt((x-x3)*(x-x3)+(y-y3)*(y-y3));
		int aveNo = Integer.parseInt(ave.getText());
		double sum1 = 0.0;
		double sum2 = 0.0;
		double sum3 = 0.0;
		int j=0;
		for(; j < aveNo && j+i < samplesWiFi1.size();j++) {
			sum1 += Math.abs(samplesWiFi1.get(i+j));
			sum2 += Math.abs(samplesWiFi2.get(i+j));
			sum3 += Math.abs(samplesWiFi3.get(i+j));
		}
		double meanPower1 = sum1/(aveNo);
		double meanPower2 = sum2/(aveNo);
		double meanPower3 = sum3/(aveNo);

		double R = meanPower*r1*r1/Math.abs(meanPower1);
		double r = meanPower*r2*r2/Math.abs(meanPower2);
		System.out.println(""+r1+","+r2+","+r3+","+meanPower+","+Math.sqrt(R)+","+Math.sqrt(r));

		//work out y and x
		double a = x1; double b = y1; double c = x2; double d = y2;
		double divisor = (2*(a - c)*(a*a + b*b - 2*a*c + c*c - 2*b*d + d*d));
		divisor = (divisor == 0.0) ? 1e-99:divisor;
		double tempy1 = (a*a*b + b*b*b - 2*a*b*c + b*c*c + a*a*d - b*b*d - 2*a*c*d + c*c*d - 
				b*d*d + d*d*d + b*r - d*r - b*R + d*R - 
				Math.sqrt((a-c)*(a-c)*(-Math.pow(a,4.0) - 2*a*a*b*b - Math.pow(b,4.0) + 4*a*a*a*c + 4*a*b*b*c - 
						6*a*a*c*c - 2*b*b*c*c + 4*a*c*c*c - Math.pow(c,4.0) + 4*a*a*b*d + 4*b*b*b*d - 
						8*a*b*c*d + 4*b*c*c*d - 2*a*a*d*d - 6*b*b*d*d + 4*a*c*d*d - 
						2*c*c*d*d + 4*b*d*d*d - Math.pow(d,4.0) + 2*a*a*r + 2*b*b*r - 4*a*c*r + 
						2*c*c*r - 4*b*d*r + 2*d*d*r - r*r + 2*a*a*R + 2*b*b*R - 
						4*a*c*R + 2*c*c*R - 4*b*d*R + 2*d*d*R + 2*r*R - r*r)))/
				(2*(a*a + b*b - 2*a*c + c*c - 2*b*d + d*d)); 
		//		  (x-a)^2 + (y-b)^2 == R
		double tempx1 = Math.sqrt(R-(tempy1-b)*(tempy1-b))+a;
		double tempy2 = (a*a*b + b*b*b - 2*a*b*c + b*c*c + a*a*d - b*b*d - 2*a*c*d + c*c*d - 
				b*d*d + d*d*d + b*r - d*r - b*R + d*R + 
				Math.sqrt((a - c)*(a-c)*(-Math.pow(a,4.0) - 2*a*a*b*b - Math.pow(b,4.0) + 4*a*a*a*c + 4*a*b*b*c - 
						6*a*a*c*c - 2*b*b*c*c + 4*a*c*c*c - Math.pow(c,4.0) + 4*a*a*b*d + 4*b*b*b*d - 
						8*a*b*c*d + 4*b*c*c*d - 2*a*a*d*d - 6*b*b*d*d + 4*a*c*d*d - 
						2*c*c*d*d + 4*b*d*d*d - Math.pow(d,4.0) + 2*a*a*r + 2*b*b*r - 4*a*c*r + 
						2*c*c*r - 4*b*d*r + 2*d*d*r - r*r + 2*a*a*R + 2*b*b*R - 
						4*a*c*R + 2*c*c*R - 4*b*d*R + 2*d*d*R + 2*r*R - r*r)))/
				(2*(a*a + b*b - 2*a*c + c*c - 2*b*d + d*d));
		double tempx2  = -Math.sqrt(R-(tempy2-b)*(tempy2-b))+a;
  
		double lastr = Math.sqrt(meanPower*r3*r3/Math.abs(meanPower3));
		;
		double newr1 = Math.sqrt((tempx1-x3)*(tempx1-x3)+(tempy1-y3)*(tempy1-y3));
		double newr2 = Math.sqrt((tempx2-x3)*(tempx2-x3)+(tempy2-y3)*(tempy2-y3));
		System.out.println("tempx1="+tempx1+", tempy1="+tempy1);

		if (Math.abs(lastr-newr1)<Math.abs(lastr-newr2)) {
			Point p = new Point();
			p.x=tempx1;p.y=tempy1;
			System.out.println("tempx1="+tempx1+", tempy1="+tempy1);
			return p;
		}
		Point p = new Point();
		p.x=tempx2;p.y=tempy2;
		System.out.println("tempx2="+tempx2+", tempy2="+tempy2);

		return p;

	}


	class PaintDemo{

		PaintDemo(){
			jfrm.setSize(1000, 1000);
			jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			doRender.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					render = true;
					NOISE_VALUE = Double.parseDouble(minx.getText());

					pe.repaint();				
				}

			});
			jfrm.setLayout(new BorderLayout());

			JPanel temp = new JPanel();
			temp.add(maxxl);		
			temp.add(maxx);
			temp.add(minxl);		
			temp.add(minx);
			temp.add(avel);		
			temp.add(ave);
			temp.add(doRender);
			//			canvas.setVisible(true);
			jfrm.add(temp, BorderLayout.NORTH);
			jfrm.add(pe, BorderLayout.CENTER);

			//jfrm.add(diamond);

			jfrm.setVisible(true);
		}

	}
	public static void main(String args[]) throws IOException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					if (args.length != 1) {
						System.out.println("usage: java PositionError /Users/charlesswires/Documents/model.csv");
						System.exit(1);
					}
					pe = new PositionError(args[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
				pe.new PaintDemo();

			}
		});
	}

}
