/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package br.edu.help.lovingyou.app.view.component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Class implementing actual curl/page rendering.
 * 
 * @author harism
 */
public class CurlMesh {

	/**
	 * Flag for rendering some lines used for developing. Shows curl position
	 * and one for the direction from the position given. Comes handy once
	 * playing around with different ways for following pointer.
	 */
	private static final boolean CURL_POSITION = false;

	/**
	 * Flag for drawing polygon outlines. Using this flag crashes on emulator
	 * due to reason unknown to me. Leaving it here anyway as seeing polygon
	 * outlines gives good insight how original rectangle is divided.
	 */
	private static final boolean POLYGON_OUTLINES = false;

	/**
	 * Flag for enabling shadow rendering.
	 */
	private static final boolean DRAW_SHADOW = true;
	/**
	 * Flag for texture rendering. While this is likely something you don't want
	 * to do it's been used for development purposes as texture rendering is
	 * rather slow on emulator.
	 */
	private static final boolean DRAW_TEXTURE = true;

	/**
	 * Colors for shadow. Inner one is the color drawn next to surface where
	 * shadowed area starts and outer one is color shadow ends to.
	 */
	private static final float[] SHW_INNER_COLOR = { 0f, 0f, 0f, .5f };
	private static final float[] SHDW_OUTER_COLOR = { 0f, 0f, 0f, .0f };

	/**
	 * Let's avoid using 'new' as much as possible. Meaning we introduce arrays
	 * once here and reuse them on runtime. Doesn't really have very much effect
	 * but avoids some garbage collections from happening.
	 */
	private transient Array<ShadowVertex> dropShwVertices;
	private transient final Array<Vertex> intersections;
	private transient final Array<Vertex> outputVertices;
	private transient final Array<Vertex> rotatedVertices;
	private transient final Array<Double> scanLines;
	private transient final Array<ShadowVertex> selfShwVertices;
	private transient final Array<ShadowVertex> tempShwVertices;
	private transient final Array<Vertex> tempVertices;

	/**
	 * Buffers for feeding rasterizer.
	 */
	private transient final FloatBuffer mBufColors;
	private transient FloatBuffer curlPositionLines;
	private transient FloatBuffer mBufShadowColors;
	private transient FloatBuffer bShadowVertices;
	private transient FloatBuffer mBufTexCoords;
	private transient final FloatBuffer mBufVertices;

	private transient int curlPosLnCount;
	private transient int dropShowCount;

	/**
	 * Boolean for 'flipping' texture sideways.
	 */
	private transient boolean mFlipTexture = false;
	/**
	 * Maximum number of split lines used for creating a curl.
	 * 
	 */
	private transient final int mMaxCurlSplits;

	/**
	 * Bounding rectangle for this mesh. mRectagle[0] = top-left corner,
	 */
	private transient final Vertex[] mRectangle = new Vertex[4];
	private transient int mSelfShadowCount;

	private transient boolean mTextureBack = false;
	/**
	 * Texture ids and other variables.
	 */
	private transient int[] baseTextureIds = null;
	private transient final CurlPage mTexturePage;
	private transient final RectF mTextureRectBack = new RectF();
	private transient final RectF mTextureRectFront = new RectF();

	private transient int vertCountBack;
	private transient int vertCountFront;
	private int[] mTextureIds;

	/**
	 * Constructor for mesh object.
	 * 
	 * @param maxCurlSplits
	 *            Maximum number curl can be divided into. The bigger the value
	 *            the smoother curl will be. With the cost of having more
	 *            polygons for drawing.
	 */
	public CurlMesh(final int maxCurlSplits) {
		mTexturePage = new CurlPage();

		// There really is no use for 0 splits.
		mMaxCurlSplits = maxCurlSplits < 1 ? 1 : maxCurlSplits;

		scanLines = new Array<Double>(maxCurlSplits + 2);
		outputVertices = new Array<Vertex>(7);
		rotatedVertices = new Array<Vertex>(4);
		intersections = new Array<Vertex>(2);
		tempVertices = new Array<Vertex>(7 + 4);
		for (int i = 0; i < 7 + 4; ++i) {
			tempVertices.add(new Vertex());
		}

		if (DRAW_SHADOW) {
			selfShwVertices = new Array<ShadowVertex>((mMaxCurlSplits + 2) * 2);
			dropShwVertices = new Array<ShadowVertex>((mMaxCurlSplits + 2) * 2);
			tempShwVertices = new Array<ShadowVertex>((mMaxCurlSplits + 2) * 2);
			for (int i = 0; i < (mMaxCurlSplits + 2) * 2; ++i) {
				tempShwVertices.add(new ShadowVertex());
			}
		}

		// Rectangle consists of 4 vertices. Index 0 = top-left, index 1 =
		// bottom-left, index 2 = top-right and index 3 = bottom-right.
		for (int i = 0; i < 4; ++i) {
			mRectangle[i] = new Vertex();
		}
		// Set up shadow penumbra direction to each vertex. We do fake 'self
		// shadow' calculations based on this information.
		mRectangle[0].mPenumbraX = mRectangle[1].mPenumbraX = mRectangle[1].mPenumbraY = mRectangle[3].mPenumbraY = -1;// NOSONAR
																														// this
																														// is
																														// external
																														// code
		mRectangle[0].mPenumbraY = mRectangle[2].mPenumbraX = mRectangle[2].mPenumbraY = mRectangle[3].mPenumbraX = 1;// NOSONAR

		if (CURL_POSITION) {
			curlPosLnCount = 3;
			final ByteBuffer hvbb = ByteBuffer
					.allocateDirect(curlPosLnCount * 2 * 2 * 4);
			hvbb.order(ByteOrder.nativeOrder());
			curlPositionLines = hvbb.asFloatBuffer();
			curlPositionLines.position(0);
		}

		/*
		 * There are 4 vertices from bounding rect, max 2 from adding split line
		 * to two corners and curl consists of max mMaxCurlSplits lines each
		 * outputting 2 vertices.
		 */
		final int maxVerticesCount = 4 + 2 + (2 * mMaxCurlSplits);
		final ByteBuffer vbb = ByteBuffer
				.allocateDirect(maxVerticesCount * 3 * 4);
		vbb.order(ByteOrder.nativeOrder());
		mBufVertices = vbb.asFloatBuffer();
		mBufVertices.position(0);

		if (DRAW_TEXTURE) {
			final ByteBuffer tbb = ByteBuffer
					.allocateDirect(maxVerticesCount * 2 * 4);
			tbb.order(ByteOrder.nativeOrder());
			mBufTexCoords = tbb.asFloatBuffer();
			mBufTexCoords.position(0);
		}

		final ByteBuffer cbb = ByteBuffer
				.allocateDirect(maxVerticesCount * 4 * 4);
		cbb.order(ByteOrder.nativeOrder());
		mBufColors = cbb.asFloatBuffer();
		mBufColors.position(0);

		if (DRAW_SHADOW) {
			final int maxShadowVertCnt = (mMaxCurlSplits + 2) * 2 * 2;
			final ByteBuffer scbb = ByteBuffer
					.allocateDirect(maxShadowVertCnt * 4 * 4);
			scbb.order(ByteOrder.nativeOrder());
			mBufShadowColors = scbb.asFloatBuffer();
			mBufShadowColors.position(0);

			final ByteBuffer sibb = ByteBuffer
					.allocateDirect(maxShadowVertCnt * 3 * 4);
			sibb.order(ByteOrder.nativeOrder());
			bShadowVertices = sibb.asFloatBuffer();
			bShadowVertices.position(0);

			dropShowCount = mSelfShadowCount = 0;
		}
	}

	/**
	 * Adds vertex to buffers.
	 */
	private void addVertex(final Vertex vertex) {
		mBufVertices.put((float) vertex.mPosX);
		mBufVertices.put((float) vertex.mPosY);
		mBufVertices.put((float) vertex.mPosZ);
		mBufColors.put(vertex.mColorFactor * Color.red(vertex.mColor) / 255f);
		mBufColors.put(vertex.mColorFactor * Color.green(vertex.mColor) / 255f);
		mBufColors.put(vertex.mColorFactor * Color.blue(vertex.mColor) / 255f);
		mBufColors.put(Color.alpha(vertex.mColor) / 255f);
		if (DRAW_TEXTURE) {
			mBufTexCoords.put((float) vertex.mTexX);
			mBufTexCoords.put((float) vertex.mTexY);
		}
	}

	/**
	 * Sets curl for this mesh.
	 * 
	 * @param curlPos
	 *            Position for curl 'center'. Can be any point on line collinear
	 *            to curl.
	 * @param curlDir
	 *            Curl direction, should be normalized.
	 * @param radius
	 *            Radius of curl.
	 */
	public synchronized void curl(final PointF curlPos, final PointF curlDir,
			final double radius) {

		// First add some 'helper' lines used for development.
		if (CURL_POSITION) {
			curlPositionLines.position(0);

			curlPositionLines.put(curlPos.x);
			curlPositionLines.put(curlPos.y - 1.0f);
			curlPositionLines.put(curlPos.x);
			curlPositionLines.put(curlPos.y + 1.0f);
			curlPositionLines.put(curlPos.x - 1.0f);
			curlPositionLines.put(curlPos.y);
			curlPositionLines.put(curlPos.x + 1.0f);
			curlPositionLines.put(curlPos.y);

			curlPositionLines.put(curlPos.x);
			curlPositionLines.put(curlPos.y);
			curlPositionLines.put(curlPos.x + curlDir.x * 2);
			curlPositionLines.put(curlPos.y + curlDir.y * 2);

			curlPositionLines.position(0);
		}

		// Actual 'curl' implementation starts here.
		mBufVertices.position(0);
		mBufColors.position(0);
		if (DRAW_TEXTURE) {
			mBufTexCoords.position(0);
		}

		// Calculate curl angle from direction.
		double curlAngle = Math.acos(curlDir.x);
		curlAngle = curlDir.y > 0 ? -curlAngle : curlAngle;

		/*
		 * Initiate rotated rectangle which's is translated to curlPos and
		 * rotated so that curl direction heads to right (1,0). Vertices are
		 * ordered in ascending order based on x -coordinate at the same time.
		 * And using y -coordinate in very rare case in which two vertices have
		 * same x -coordinate.
		 */
		tempVertices.addAll(rotatedVertices);
		rotatedVertices.clear();
		for (int i = 0; i < 4; ++i) {
			final Vertex vertex = tempVertices.remove(0);
			vertex.set(mRectangle[i]);
			vertex.translate(-curlPos.x, -curlPos.y);
			vertex.rotateZ(-curlAngle);
			int jjj = 0;
			for (; jjj < rotatedVertices.size(); ++jjj) {
				final Vertex vertex2 = rotatedVertices.get(jjj);
				if (vertex.mPosX > vertex2.mPosX) {
					break;
				}
				if (vertex.mPosX == vertex2.mPosX
						&& vertex.mPosY > vertex2.mPosY) {
					break;
				}
			}
			rotatedVertices.add(jjj, vertex);
		}

		/*
		 * Rotated rectangle lines/vertex indices. We need to find bounding
		 * lines for rotated rectangle. After sorting vertices according to
		 * their x -coordinate we don't have to worry about vertices at indices
		 * 0 and 1. But due to inaccuracy it's possible vertex 3 is not the
		 * opposing corner from vertex 0. So we are calculating distance from
		 * vertex 0 to vertices 2 and 3 - and altering line indices if needed.
		 * Also vertices/lines are given in an order first one has x -coordinate
		 * at least the latter one. This property is used in getIntersections to
		 * see if there is an intersection.
		 */
		final int lines[][] = { { 0, 1 }, { 0, 2 }, { 1, 3 }, { 2, 3 } };
		{
			/*
			 * TODO: There really has to be more 'easier' way of doing this -
			 * not including extensive use of sqrt.
			 */
			final Vertex vertex0 = rotatedVertices.get(0);
			final Vertex vertex2 = rotatedVertices.get(2);
			final Vertex vertex3 = rotatedVertices.get(3);
			final double dist2 = Math.sqrt((vertex0.mPosX - vertex2.mPosX)
					* (vertex0.mPosX - vertex2.mPosX)
					+ (vertex0.mPosY - vertex2.mPosY)
					* (vertex0.mPosY - vertex2.mPosY));
			final double dist3 = Math.sqrt((vertex0.mPosX - vertex3.mPosX)
					* (vertex0.mPosX - vertex3.mPosX)
					+ (vertex0.mPosY - vertex3.mPosY)
					* (vertex0.mPosY - vertex3.mPosY));
			if (dist2 > dist3) {
				lines[1][1] = 3;
				lines[2][1] = 2;
			}
		}

		vertCountFront = vertCountBack = 0;

		if (DRAW_SHADOW) {
			tempShwVertices.addAll(dropShwVertices);
			tempShwVertices.addAll(selfShwVertices);
			dropShwVertices.clear();
			selfShwVertices.clear();
		}

		// Length of 'curl' curve.
		final double curlLength = Math.PI * radius;
		// Calculate scan lines.
		// TODO: Revisit this code one day. There is room for optimization here.
		scanLines.clear();
		if (mMaxCurlSplits > 0) {
			scanLines.add((double) 0);
		}
		for (int i = 1; i < mMaxCurlSplits; ++i) {
			scanLines.add((-curlLength * i) / (mMaxCurlSplits - 1));
		}
		/*
		 * As mRotatedVertices is ordered regarding x -coordinate, adding this
		 * scan line produces scan area picking up vertices which are rotated
		 * completely. One could say 'until infinity'.
		 */
		scanLines.add(rotatedVertices.get(3).mPosX - 1);

		/*
		 * Start from right most vertex. Pretty much the same as first scan area
		 * is starting from 'infinity'.
		 */
		double scanXmax = rotatedVertices.get(0).mPosX + 1;

		for (int i = 0; i < scanLines.size(); ++i) {
			// Once we have scanXmin and scanXmax we have a scan area to start
			// working with.
			final double scanXmin = scanLines.get(i);
			// First iterate 'original' rectangle vertices within scan area.
			for (int jjj = 0; jjj < rotatedVertices.size(); ++jjj) {
				final Vertex vertex = rotatedVertices.get(jjj);
				// Test if vertex lies within this scan area.
				/*
				 * TODO: Frankly speaking, can't remember why equality check was
				 * added to both ends. Guessing it was somehow related to case
				 * where radius=0f, which, given current implementation, could
				 * be handled much more effectively anyway.
				 */
				if (vertex.mPosX >= scanXmin && vertex.mPosX <= scanXmax) {
					// Pop out a vertex from temp vertices.
					final Vertex n = tempVertices.remove(0);
					n.set(vertex);
					/*
					 * This is done solely for triangulation reasons. Given a
					 * rotated rectangle it has max 2 vertices having
					 * intersection.
					 */
					final Array<Vertex> intersections = getIntersections(
							rotatedVertices, lines, n.mPosX);
					/*
					 * In a sense one could say we're adding vertices always in
					 * two, positioned at the ends of intersecting line. And for
					 * triangulation to work properly they are added based on y
					 * -coordinate. And this if-else is doing it for us.
					 */
					if (intersections.size() == 1
							&& intersections.get(0).mPosY > vertex.mPosY) {
						// In case intersecting vertex is higher add it first.
						outputVertices.addAll(intersections);
						outputVertices.add(n);
					} else if (intersections.size() <= 1) {
						// Otherwise add original vertex first.
						outputVertices.add(n);
						outputVertices.addAll(intersections);
					} else {
						/*
						 * There should never be more than 1 intersecting
						 * vertex. But if it happens as a fallback simply skip
						 * everything.
						 */
						tempVertices.add(n);
						tempVertices.addAll(intersections);
					}
				}
			}

			// Search for scan line intersections.
			final Array<Vertex> intersections = getIntersections(
					rotatedVertices, lines, scanXmin);

			/*
			 * We expect to get 0 or 2 vertices. In rare cases there's only one
			 * but in general given a scan line intersecting rectangle there
			 * should be 2 intersecting vertices.
			 */
			if (intersections.size() == 2) {
				// There were two intersections, add them based on y
				// -coordinate, higher first, lower last.
				final Vertex v1 = intersections.get(0);
				final Vertex v2 = intersections.get(1);
				if (v1.mPosY < v2.mPosY) {
					outputVertices.add(v2);
					outputVertices.add(v1);
				} else {
					outputVertices.addAll(intersections);
				}
			} else if (intersections.size() != 0) {
				/*
				 * This happens in a case in which there is a original vertex
				 * exactly at scan line or something went very much wrong if
				 * there are 3+ vertices. What ever the reason just return the
				 * vertices to temp vertices for later use. In former case it
				 * was handled already earlier once iterating through
				 * mRotatedVertices, in latter case it's better to avoid doing
				 * anything with them.
				 */
				tempVertices.addAll(intersections);
			}

			// Add vertices found during this iteration to vertex etc buffers.
			while (outputVertices.size() > 0) {
				final Vertex v = outputVertices.remove(0);
				tempVertices.add(v);

				// Local texture front-facing flag.
				boolean textureFront;

				// Untouched vertices.
				if (i == 0) {
					textureFront = true;
					vertCountFront++;
				}
				// 'Completely' rotated vertices.
				else if (i == scanLines.size() - 1 || curlLength == 0) {
					v.mPosX = -(curlLength + v.mPosX);
					v.mPosZ = 2 * radius;
					v.mPenumbraX = -v.mPenumbraX;

					textureFront = false;
					vertCountBack++;
				}
				// Vertex lies within 'curl'.
				else {
					/*
					 * Even though it's not obvious from the if-else clause,
					 * here v.mPosX is between [-curlLength, 0]. And we can do
					 * calculations around a half cylinder.
					 */
					final double rotY = Math.PI * (v.mPosX / curlLength);
					v.mPosX = radius * Math.sin(rotY);
					v.mPosZ = radius - (radius * Math.cos(rotY));
					v.mPenumbraX *= Math.cos(rotY);
					// Map color multiplier to [.1f, 1f] range.
					v.mColorFactor = (float) (.1f + .9f * Math.sqrt(Math
							.sin(rotY) + 1));

					if (v.mPosZ >= radius) {
						textureFront = false;
						vertCountBack++;
					} else {
						textureFront = true;
						vertCountFront++;
					}
				}

				/*
				 * We use local textureFront for flipping backside texture
				 * locally. Plus additionally if mesh is in flip texture mode,
				 * we'll make the procedure "backwards". Also, until this point,
				 * texture coordinates are within [0, 1] range so we'll adjust
				 * them to final texture coordinates too.
				 */
				if (textureFront != mFlipTexture) {
					v.mTexX *= mTextureRectFront.right;
					v.mTexY *= mTextureRectFront.bottom;
					v.mColor = mTexturePage.getColor(CurlPage.SIDE_FRONT);
				} else {
					v.mTexX *= mTextureRectBack.right;
					v.mTexY *= mTextureRectBack.bottom;
					v.mColor = mTexturePage.getColor(CurlPage.SIDE_BACK);
				}

				// Move vertex back to 'world' coordinates.
				v.rotateZ(curlAngle);
				v.translate(curlPos.x, curlPos.y);
				addVertex(v);

				// Drop shadow is cast 'behind' the curl.
				if (DRAW_SHADOW && v.mPosZ > 0 && v.mPosZ <= radius) {
					final ShadowVertex sv = tempShwVertices.remove(0);
					sv.mPosX = v.mPosX;
					sv.mPosY = v.mPosY;
					sv.mPosZ = v.mPosZ;
					sv.mPenumbraX = (v.mPosZ / 2) * -curlDir.x;
					sv.mPenumbraY = (v.mPosZ / 2) * -curlDir.y;
					sv.mPenumbraColor = v.mPosZ / radius;
					final int idx = (dropShwVertices.size() + 1) / 2;
					dropShwVertices.add(idx, sv);
				}
				// Self shadow is cast partly over mesh.
				if (DRAW_SHADOW && v.mPosZ > radius) {
					final ShadowVertex sv = tempShwVertices.remove(0);
					sv.mPosX = v.mPosX;
					sv.mPosY = v.mPosY;
					sv.mPosZ = v.mPosZ;
					sv.mPenumbraX = ((v.mPosZ - radius) / 3) * v.mPenumbraX;
					sv.mPenumbraY = ((v.mPosZ - radius) / 3) * v.mPenumbraY;
					sv.mPenumbraColor = (v.mPosZ - radius) / (2 * radius);
					final int idx = (selfShwVertices.size() + 1) / 2;
					selfShwVertices.add(idx, sv);
				}
			}

			// Switch scanXmin as scanXmax for next iteration.
			scanXmax = scanXmin;
		}

		mBufVertices.position(0);
		mBufColors.position(0);
		if (DRAW_TEXTURE) {
			mBufTexCoords.position(0);
		}

		// Add shadow Vertices.
		if (DRAW_SHADOW) {
			mBufShadowColors.position(0);
			bShadowVertices.position(0);
			dropShowCount = 0;

			for (int i = 0; i < dropShwVertices.size(); ++i) {
				final ShadowVertex sv = dropShwVertices.get(i);
				bShadowVertices.put((float) sv.mPosX);
				bShadowVertices.put((float) sv.mPosY);
				bShadowVertices.put((float) sv.mPosZ);
				bShadowVertices.put((float) (sv.mPosX + sv.mPenumbraX));
				bShadowVertices.put((float) (sv.mPosY + sv.mPenumbraY));
				bShadowVertices.put((float) sv.mPosZ);
				for (int j = 0; j < 4; ++j) {
					final double color = SHDW_OUTER_COLOR[j]
							+ (SHW_INNER_COLOR[j] - SHDW_OUTER_COLOR[j])
							* sv.mPenumbraColor;
					mBufShadowColors.put((float) color);
				}
				mBufShadowColors.put(SHDW_OUTER_COLOR);
				dropShowCount += 2;
			}
			mSelfShadowCount = 0;
			for (int i = 0; i < selfShwVertices.size(); ++i) {
				final ShadowVertex sv = selfShwVertices.get(i);
				bShadowVertices.put((float) sv.mPosX);
				bShadowVertices.put((float) sv.mPosY);
				bShadowVertices.put((float) sv.mPosZ);
				bShadowVertices.put((float) (sv.mPosX + sv.mPenumbraX));
				bShadowVertices.put((float) (sv.mPosY + sv.mPenumbraY));
				bShadowVertices.put((float) sv.mPosZ);
				for (int j = 0; j < 4; ++j) {
					final double color = SHDW_OUTER_COLOR[j]
							+ (SHW_INNER_COLOR[j] - SHDW_OUTER_COLOR[j])
							* sv.mPenumbraColor;
					mBufShadowColors.put((float) color);
				}
				mBufShadowColors.put(SHDW_OUTER_COLOR);
				mSelfShadowCount += 2;
			}
			mBufShadowColors.position(0);
			bShadowVertices.position(0);
		}
	}

	/**
	 * Calculates intersections for given scan line.
	 */
	private Array<Vertex> getIntersections(final Array<Vertex> vertices,
			final int[][] lineIndices, final double scanX) {
		intersections.clear();
		// Iterate through rectangle lines each re-presented as a pair of
		// vertices.
		for (int j = 0; j < lineIndices.length; j++) {
			final Vertex v1 = vertices.get(lineIndices[j][0]);
			final Vertex v2 = vertices.get(lineIndices[j][1]);
			// Here we expect that v1.mPosX >= v2.mPosX and wont do intersection
			// test the opposite way.
			if (v1.mPosX > scanX && v2.mPosX < scanX) {
				// There is an intersection, calculate coefficient telling 'how
				// far' scanX is from v2.
				final double c = (scanX - v2.mPosX) / (v1.mPosX - v2.mPosX);
				final Vertex n = tempVertices.remove(0);
				n.set(v2);
				n.mPosX = scanX;
				n.mPosY += (v1.mPosY - v2.mPosY) * c;
				if (DRAW_TEXTURE) {
					n.mTexX += (v1.mTexX - v2.mTexX) * c;
					n.mTexY += (v1.mTexY - v2.mTexY) * c;
				}
				if (DRAW_SHADOW) {
					n.mPenumbraX += (v1.mPenumbraX - v2.mPenumbraX) * c;
					n.mPenumbraY += (v1.mPenumbraY - v2.mPenumbraY) * c;
				}
				intersections.add(n);
			}
		}
		return intersections;
	}

	/**
	 * Getter for textures page for this mesh.
	 */
	public synchronized CurlPage getTexturePage() {
		return mTexturePage;
	}

	/**
	 * Renders our page curl mesh.
	 */
	public synchronized void onDrawFrame(final GL10 gl) {
		// First allocate texture if there is not one yet.

		if (DRAW_TEXTURE && baseTextureIds == null) {
			// Generate texture.
			mTextureIds = new int[2];
			gl.glGenTextures(2, mTextureIds, 0);
			baseTextureIds = new int[2];
			baseTextureIds[0] = mTextureIds[0];
			baseTextureIds[1] = mTextureIds[1];
			for (final int textureId : mTextureIds) {
				// Set texture attributes.
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D,
						GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
						GL10.GL_CLAMP_TO_EDGE);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
						GL10.GL_CLAMP_TO_EDGE);
			}
		}

		if (mTexturePage.getRectTexture() != null) {
			mTextureRectFront.set(mTexturePage.getRectTexture());
		}

		if (mTexturePage.getRectTexture() != null) {
			mTextureRectBack.set(mTexturePage.getRectTexture());
		}

		if (DRAW_TEXTURE && mTexturePage.isTexturesChanged()) {

			if (mTexturePage.getTextureIdFront() != null) {
				baseTextureIds[0] = mTexturePage.getTextureIdFront();
			} else {
				baseTextureIds[0] = mTextureIds[0];
			}

			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureIds[0]);

			if (mTexturePage.getRectTexture() != null) {
				mTextureRectFront.set(mTexturePage.getRectTexture());
			} else {
				mTextureRectFront.set(0, 0, .1f, .1f);
			}

			mTextureBack = mTexturePage.hasBackTexture();
			if (mTextureBack) {

				if (mTexturePage.getTextureIdBack() != null) {
					baseTextureIds[1] = mTexturePage.getTextureIdBack();
				} else {
					baseTextureIds[1] = mTextureIds[1];
				}

				if (mTexturePage.getRectTexture() != null) {
					mTextureRectBack.set(mTexturePage.getRectTexture());
				} else {
					mTextureRectBack.set(0, 0, 1, 1);
				}

				gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureIds[1]);

			} else {
				mTextureRectBack.set(mTextureRectFront);
			}

			mTexturePage.recycle();
			reset();
		}

		// Some 'global' settings.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// TODO: Drop shadow drawing is done temporarily here to hide some
		// problems with its calculation.
		if (DRAW_SHADOW) {
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, mBufShadowColors);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bShadowVertices);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, dropShowCount);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glDisable(GL10.GL_BLEND);
		}

		if (DRAW_TEXTURE) {
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mBufTexCoords);
		}
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufVertices);
		// Enable color array.
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, mBufColors);

		// Draw front facing blank vertices.
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertCountFront);

		// Draw front facing texture.
		if (DRAW_TEXTURE) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glEnable(GL10.GL_TEXTURE_2D);

			if (!mFlipTexture || !mTextureBack) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, baseTextureIds[0]);
			} else {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, baseTextureIds[1]);
			}

			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertCountFront);

			gl.glDisable(GL10.GL_BLEND);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}

		final int backStartIdx = Math.max(0, vertCountFront - 2);
		final int backCount = vertCountFront + vertCountBack - backStartIdx;

		// Draw back facing blank vertices.
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, backStartIdx, backCount);

		// Draw back facing texture.
		if (DRAW_TEXTURE) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glEnable(GL10.GL_TEXTURE_2D);

			if (mFlipTexture || !mTextureBack) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, baseTextureIds[0]);
			} else {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, baseTextureIds[1]);
			}

			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, backStartIdx, backCount);

			gl.glDisable(GL10.GL_BLEND);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}

		// Disable textures and color array.
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

		if (POLYGON_OUTLINES) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glLineWidth(1.0f);
			gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufVertices);
			gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, vertCountFront);
			gl.glDisable(GL10.GL_BLEND);
		}

		if (CURL_POSITION) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glLineWidth(1.0f);
			gl.glColor4f(1.0f, 0.5f, 0.5f, 1.0f);
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, curlPositionLines);
			gl.glDrawArrays(GL10.GL_LINES, 0, curlPosLnCount * 2);
			gl.glDisable(GL10.GL_BLEND);
		}

		if (DRAW_SHADOW) {
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, mBufShadowColors);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bShadowVertices);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, dropShowCount,
					mSelfShadowCount);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glDisable(GL10.GL_BLEND);
		}

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}

	/**
	 * Resets mesh to 'initial' state. Meaning this mesh will draw a plain
	 * textured rectangle after call to this method.
	 */
	public synchronized void reset() {
		mBufVertices.position(0);
		mBufColors.position(0);
		if (DRAW_TEXTURE) {
			mBufTexCoords.position(0);
		}
		for (int i = 0; i < 4; ++i) {
			final Vertex tmp = tempVertices.get(0);
			tmp.set(mRectangle[i]);

			if (mFlipTexture) {
				tmp.mTexX *= mTextureRectBack.right;
				tmp.mTexY *= mTextureRectBack.bottom;
				tmp.mColor = mTexturePage.getColor(CurlPage.SIDE_BACK);
			} else {
				tmp.mTexX *= mTextureRectFront.right;
				tmp.mTexY *= mTextureRectFront.bottom;
				tmp.mColor = mTexturePage.getColor(CurlPage.SIDE_FRONT);
			}

			addVertex(tmp);
		}
		vertCountFront = 4;
		vertCountBack = 0;
		mBufVertices.position(0);
		mBufColors.position(0);
		if (DRAW_TEXTURE) {
			mBufTexCoords.position(0);
		}

		dropShowCount = mSelfShadowCount = 0;
	}

	/**
	 * Resets allocated texture id forcing creation of new one. After calling
	 * this method you most likely want to set bitmap too as it's lost. This
	 * method should be called only once e.g GL context is re-created as this
	 * method does not release previous texture id, only makes sure new one is
	 * requested on next render.
	 */
	public synchronized void resetTexture() {
		baseTextureIds = null;
	}

	/**
	 * If true, flips texture sideways.
	 */
	public synchronized void setFlipTexture(final boolean flipTexture) {
		mFlipTexture = flipTexture;
		if (flipTexture) {
			setTexCoords(1f, 0f, 0f, 1f);
		} else {
			setTexCoords(0f, 0f, 1f, 1f);
		}
	}

	/**
	 * Update mesh bounds.
	 */
	public void setRect(final RectF r) {
		mRectangle[0].mPosX = r.left;
		mRectangle[0].mPosY = r.top;
		mRectangle[1].mPosX = r.left;
		mRectangle[1].mPosY = r.bottom;
		mRectangle[2].mPosX = r.right;
		mRectangle[2].mPosY = r.top;
		mRectangle[3].mPosX = r.right;
		mRectangle[3].mPosY = r.bottom;
	}

	/**
	 * Sets texture coordinates to mRectangle vertices.
	 */
	private synchronized void setTexCoords(final float left, final float top,
			final float right, final float bottom) {
		mRectangle[0].mTexX = left;
		mRectangle[0].mTexY = top;
		mRectangle[1].mTexX = left;
		mRectangle[1].mTexY = bottom;
		mRectangle[2].mTexX = right;
		mRectangle[2].mTexY = top;
		mRectangle[3].mTexX = right;
		mRectangle[3].mTexY = bottom;
	}

	/**
	 * Simple fixed size array implementation.
	 */
	private class Array<T> {
		private transient final Object[] mArray;
		private transient final int mCapacity;
		private transient int mSize;

		public Array(final int capacity) {
			mCapacity = capacity;
			mArray = new Object[capacity];
		}

		public void add(final int index, final T item) {
			if (index < 0 || index > mSize || mSize >= mCapacity) {
				throw new IndexOutOfBoundsException();
			}
			for (int i = mSize; i > index; --i) {
				mArray[i] = mArray[i - 1];
			}
			mArray[index] = item;
			++mSize;
		}

		public void add(final T item) {
			if (mSize >= mCapacity) {
				throw new IndexOutOfBoundsException();
			}
			mArray[mSize++] = item;
		}

		public void addAll(final Array<T> array) {
			if (mSize + array.size() > mCapacity) {
				throw new IndexOutOfBoundsException();
			}
			for (int i = 0; i < array.size(); ++i) {
				mArray[mSize++] = array.get(i);
			}
		}

		public void clear() {
			mSize = 0;
		}

		@SuppressWarnings("unchecked")
		public T get(final int index) {
			if (index < 0 || index >= mSize) {
				throw new IndexOutOfBoundsException();
			}
			return (T) mArray[index];
		}

		@SuppressWarnings("unchecked")
		public T remove(final int index) {
			if (index < 0 || index >= mSize) {
				throw new IndexOutOfBoundsException();
			}
			final T item = (T) mArray[index];
			for (int i = index; i < mSize - 1; ++i) {
				mArray[i] = mArray[i + 1];
			}
			--mSize;
			return item;
		}

		public int size() {
			return mSize;
		}

	}

	/**
	 * Holder for shadow vertex information.
	 */
	private class ShadowVertex {
		public double mPenumbraColor;
		public double mPenumbraX;
		public double mPenumbraY;
		public double mPosX;
		public double mPosY;
		public double mPosZ;

	}

	/**
	 * Holder for vertex information.
	 */
	private class Vertex {
		public transient int mColor;
		public transient float mColorFactor;
		public transient double mPenumbraX;
		public transient double mPenumbraY;
		public transient double mPosX;
		public transient double mPosY;
		public transient double mPosZ;
		public transient double mTexX;
		public transient double mTexY;

		/**
		 * Public Constructor
		 */
		public Vertex() {
			mPosX = mPosY = mPosZ = mTexX = mTexY = 0;
			mColorFactor = 1.0f;
		}

		/**
		 * Rotate the page
		 * @param theta
		 */
		public void rotateZ(final double theta) {
			final double cos = Math.cos(theta);
			final double sin = Math.sin(theta);
			final double x = mPosX * cos + mPosY * sin;
			final double y = mPosX * -sin + mPosY * cos;
			mPosX = x;
			mPosY = y;
			final double px = mPenumbraX * cos + mPenumbraY * sin;
			final double py = mPenumbraX * -sin + mPenumbraY * cos;
			mPenumbraX = px;
			mPenumbraY = py;
		}

		/**
		 * Sets the currently vertesxt
		 * @param vertex
		 */
		public void set(final Vertex vertex) {
			mPosX = vertex.mPosX;
			mPosY = vertex.mPosY;
			mPosZ = vertex.mPosZ;
			mTexX = vertex.mTexX;
			mTexY = vertex.mTexY;
			mPenumbraX = vertex.mPenumbraX;
			mPenumbraY = vertex.mPenumbraY;
			mColor = vertex.mColor;
			mColorFactor = vertex.mColorFactor;
		}

		/**
		 * Translate the position numbers
		 */
		public void translate(final double dx, final double dy) {
			mPosX += dx;
			mPosY += dy;
		}
	}
}
