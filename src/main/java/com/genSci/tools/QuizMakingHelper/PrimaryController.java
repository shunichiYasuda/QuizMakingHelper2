package com.genSci.tools.QuizMakingHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;

public class PrimaryController {
	String encode = null;
	String sysEncode = null;
	String[] itemArray = { "Moodle 穴埋め:多肢選択", "Moodle 穴埋め:単純な数値入力", "Moodle 穴埋め：数値選択" };
	ObservableList<String> availableChoices = FXCollections.observableArrayList(itemArray);
	List<String> questionList = new ArrayList<String>();
	String path = "";
	@FXML
	TextArea srcArea;

	@FXML
	TextArea codeArea;
	@FXML
	ChoiceBox<String> choice;

	@FXML
	private void clearSrcArea() {
		srcArea.clear();
	}

	@FXML
	private void clearCodeArea() {
		codeArea.clear();
	}

	@FXML
	private void saveAction() {
		// システム文字コード
		sysEncode = System.getProperty("file.encoding");
		FileChooser fc = new FileChooser();
		fc.setTitle("Set save file");
		if (path == "") {
			fc.setInitialDirectory(new File("."));
		} else {
			fc.setInitialDirectory(new File(path));
		}
		File file = fc.showSaveDialog(null);
		if (file == null) {
			return;
		}
		path = file.getParent();
		if (encode.equals(null)) {
			encode = sysEncode;
		}
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), encode);
			BufferedWriter bw = new BufferedWriter(osw);
			PrintWriter pw = new PrintWriter(bw);
			pw.println(srcArea.getText());
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	private void openAction() {
		// システム文字コード
		sysEncode = System.getProperty("file.encoding");
		FileChooser fc = new FileChooser();
		fc.setTitle("Open data file");
		if (path == "") {
			fc.setInitialDirectory(new File("."));
		} else {
			fc.setInitialDirectory(new File(path));
		}
		File file = fc.showOpenDialog(null);
		if (file == null) {
			return;
		}
		// ファイルのパス
		path = file.getParent();
		//System.out.println("path="+path);
		// 文字コード判別
		try {
			encode = detectEncoding(file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (encode == null) {
			encode = sysEncode;
		}
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), encode);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				srcArea.appendText(line + "\n");
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	private void copyToClipboad() {
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		final ClipboardContent content = new ClipboardContent();
		content.putString(codeArea.getText());
		clipboard.setContent(content);
	}

	@FXML
	private void initialize() {
		choice.setItems(availableChoices);
		choice.setValue(availableChoices.get(0));
	}

	@FXML
	private void quitAction() {
		System.exit(0);
	}

	@FXML
	private void execAction() {
		// choice ボックス値で処理を分ける。
		String selected = choice.getValue();
		if (selected == null)
			return;

		if (selected.equals(itemArray[0])) {
			translateToMulti();
		}
		if (selected.equals(itemArray[1])) {
			translateToSimpleMulti();
		}
		if (selected.equals(itemArray[2])) {
			translateToNum();
		}
	}

	//
	private void translateToSimpleMulti() {
		// <Q> </Q> で問題ごとに区切られていることを前提
		String doc = srcArea.getText();
		doc = doc.replaceAll("\n", "");
		doc = doc.replaceAll("\t", "");
		//System.out.println("in simple");
		// codeArea.appendText(doc);
		List<String> quizList = new ArrayList<String>();
		String regex = "<Q>(.+?)</Q>";
		// regex = "(.+?)<s:.+?>";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(doc);
		while (m.find()) {
			String str = m.group();
			str = str.replace("<Q>", "<p>");
			str = str.replace("</Q>", "</p>");
			quizList.add(str);
		}
		for (String target : quizList) {
			//System.out.println("問題：" + target);
			// 単純数値問題<qn:...> を置き換える。
			p = Pattern.compile("<qn:(.+?)>");
			m = p.matcher(target);
			while (m.find()) {
				String str = m.group();
				// System.out.println(str);
				String newStr = "{1:NM:=" + m.group(1) + "}";
				target = target.replace(str, newStr);
			}
			// System.out.println("qn変換後：" + target);
			codeArea.appendText(target + "\n");
		}
	}

	private void translateToMulti() {
		// <Q> </Q> で問題ごとに区切られていることを前提
		String doc = srcArea.getText();
		doc = doc.replaceAll("\n", "");
		doc = doc.replaceAll("\t", "");
		// codeArea.appendText(doc);
		List<String> quizList = new ArrayList<String>();
		String regex = "<Q>(.+?)</Q>";
		// regex = "(.+?)<s:.+?>";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(doc);
		while (m.find()) {
			String str = m.group();
			// System.out.println("<Q>置き換え前"+str);
			// codeArea.appendText("<Q>置き換え前"+str+"\n");
			str = str.replace("<Q>", "<p>");
			str = str.replace("</Q>", "</p>");
			// String str = m.group();
			// System.out.println("in find()"+str);
			quizList.add(str);
		}
		// <p></p>で囲まれた問題ごとに処理する。
		// 多肢選択問題の場合
		for (String target : quizList) {
			// System.out.println("問題：" + target);
			// codeArea.appendText("問題："+target+"\n");
			// <s:....>までの <q: ...>を処理したい
			regex = "(.+?)<s:.+?>";
			p = Pattern.compile(regex);
			m = p.matcher(target);
			String selectItems = null;
			while (m.find()) {
				String str = m.group();
				// str は<s:...>までの文字列
				// System.out.println("最初のstr:" + str);
				String originalStr = new String(str);
				// いったん<s:....>を抜き出す。
				Pattern p_s = Pattern.compile("<s:.+?>");
				Matcher m_s = p_s.matcher(str);
				while (m_s.find()) {
					selectItems = m_s.group();
					// System.out.println("selectItems:"+selectItems);
					// codeArea.appendText("selectitems:"+selectItems+"\n");
				}
				// 選択肢 String 配列
				String newSelectItems = selectItems.replace("<s:", "");
				newSelectItems = newSelectItems.replace(">", "");
				String[] selectArray = newSelectItems.split(",");
				String regex2 = "<q:.+?>";
				Pattern p2 = Pattern.compile(regex2);
				Matcher m2 = p2.matcher(str);
				while (m2.find()) {
					String q_str = m2.group();
					// System.out.println("q_str ="+q_str);
					String ans = q_str.replace("<q:", "");
					ans = ans.replace(">", "");
					ans = ans.trim();
					// System.out.println("正答 = " + ans);
					// 選択肢配列の内容とまっちしたら
					// "=" をつけた配列に変換する。変換した配列を作っておく
					String[] tmpSelectArray = new String[selectArray.length];
					for (int j = 0; j < selectArray.length; j++) {
						tmpSelectArray[j] = selectArray[j];
						if (selectArray[j].equals(ans)) {
							tmpSelectArray[j] = "=" + selectArray[j];
						}
					}
					// 付加するMoodle Cloze 用選択肢配列を作る。
					String moodleStr = "";
					moodleStr += tmpSelectArray[0];
					for (int j = 1; j < tmpSelectArray.length; j++) {
						String tmpStr = tmpSelectArray[j];
						moodleStr += "~" + tmpStr;
					} // end of for( int j=1 . moodleStr のさくせい
						// check
						// System.out.println(moodleStr);
					String newStr = "{1:MC:" + moodleStr + "}";
					// System.out.println("q_str:" + q_str);
					// System.out.println("q置き換え前str:" + str);
					// 問題全体で<q:..> を置き換える
					str = str.replace(q_str, newStr);
				} // end of while(m2...
					// System.out.println("消去前：" + str);

				// codeArea.appendText("消去前："+str+"\n");
				// 小問題末尾の<s:...>を消す
				str = str.replace(selectItems, "");
				// System.out.println("消去後str:" + str);
				target = target.replace(originalStr, str);
				// System.out.println("置き換え後target:" + target);

				// codeArea.appendText(str+"\n");
				// System.out.println("----小問題終わり---");
				// codeArea.appendText("----小問題終わり---\n");
			} // end of while(m..小問題ごとの処理
			codeArea.appendText(target + "\n");
		}
	}

	private void translateToNum() {
		// <Q> </Q> で問題ごとに区切られていることを前提
		String doc = srcArea.getText();
		doc = doc.replaceAll("\n", "");
		doc = doc.replaceAll("\t", "");
		// codeArea.appendText(doc);
		List<String> quizList = new ArrayList<String>();
		String regex = "<Q>(.+?)</Q>";
		// regex = "(.+?)<s:.+?>";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(doc);
		while (m.find()) {
			String str = m.group();
			str = str.replace("<Q>", "<p>");
			str = str.replace("</Q>", "</p>\n");
			quizList.add(str);
		}
		// <p></p>で囲まれた問題ごとに処理する。
		for (String target : quizList) {
			// <s:...> までの小問に分割する
			// <s:....>までの <q: ...>を処理したい
			regex = "(.+?)<s:.+?>";
			p = Pattern.compile(regex);
			m = p.matcher(target);
			String selectItems = null;
			String[] selectItemArray = null;
			// 数値問題入力において<s:...>が存在したら、それは「選択肢から番号を選んで入力」を意味する。
			// そのときは<table>をつくるのでフラグが必要
			boolean tableFlag = false;
			while (m.find()) { // <s:...>で終わる小問ごとの処理
				// ここへ来たと言うことは<s:...>があったということ
				tableFlag = true;
				String str = m.group();
				// str は<s:...>までの文字列
				// System.out.println("最初のstr:" + str);
				String originalStr = new String(str);
				// いったん<s:....>を抜き出す。
				Pattern p_s = Pattern.compile("<s:(.+?)>");
				Matcher m_s = p_s.matcher(str);
				while (m_s.find()) {
					selectItems = m_s.group(1);
					// System.out.println("selectItems:"+selectItems);
					selectItemArray = selectItems.split(",");
					// codeArea.appendText("selectitems:"+selectItems+"\n");
					String oldStr = m_s.group();
					str = str.replace(oldStr, "");
				}
				// 確認
//						for (String s : selectItemArray) {
//							System.out.println(s);
//						}
				// System.out.println("消去後:" + str);
				// selectItemArray をシャッフル
				List<String> tmpList = new ArrayList<String>(Arrays.asList(selectItemArray));
				Collections.shuffle(tmpList);
				selectItemArray = tmpList.toArray(new String[tmpList.size()]);
//						for (String s : selectItemArray) {
//							System.out.println(s);
//						}
				// <q:...>の処理
				String regex2 = "<q:(.+?)>";
				Pattern p2 = Pattern.compile(regex2);
				Matcher m2 = p2.matcher(str);
				while (m2.find()) {
					String oldStr = m2.group();
					String ans = m2.group(1);
					int index = 0;
					for (int i = 0; i < selectItemArray.length; i++) {
						if (ans.equals(selectItemArray[i]))
							index = (i + 1);
					}
					String newStr = "{1:NM:=" + index + "}";
					str = str.replace(oldStr, newStr);
				}
				// System.out.println("変換後:" + str);
				target = target.replace(originalStr, str);
				//System.out.println("q: 変換後: " + target);
				//codeArea.appendText(target);
			} // end of while(m.find()) :小問ごとの処理
			codeArea.appendText(target);
			// <s:....>を<table>にする。
			if (tableFlag) {
				String tableStr = "<table>\n<caption><b>選択肢</b></caption>\n";
				// 選択肢の数
				int num = selectItemArray.length;
				int index = 0;
				tableStr += ("<tr>");
				while (index < num) {
					tableStr += "<td>" + (index + 1) + ". " + selectItemArray[index] + "</td>";
					if (((index + 1) % 5) == 0) {
						tableStr += ("</tr>\n");
						tableStr += ("<tr>");
					}
					if (index == num - 1) {
						tableStr += ("</tr>\n");
					}
					index++;
				}
				tableStr += ("</table>\n");
				// System.out.println(tableStr);
				codeArea.appendText(tableStr);
			} // end of if(tableFlag : <table>作成処理
		} // end of for( target : 問題ごとの処理
	}

	//
	// 文字コードチェック
	private String detectEncoding(File file) throws IOException {
		String result = null;
		byte[] buf = new byte[4096];
		FileInputStream fis = new FileInputStream(file);
		UniversalDetector detector = new UniversalDetector(null);
		int nread;
		while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
			detector.handleData(buf, 0, nread);
		}
		detector.dataEnd();
		result = detector.getDetectedCharset();
		detector.reset();
		fis.close();
		return result;

	}
}
