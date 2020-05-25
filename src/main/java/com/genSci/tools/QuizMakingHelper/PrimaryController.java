package com.genSci.tools.QuizMakingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

public class PrimaryController {
	String[] itemArray = { "Tex", "Cloze:Multi", "Cloze:Num" };
	ObservableList<String> availableChoices = FXCollections.observableArrayList(itemArray);
	List<String> questionList = new ArrayList<String>();
	@FXML
	TextArea srcArea;
	@FXML
	TextArea log;
	@FXML
	TextArea codeArea;
	@FXML
	ChoiceBox<String> choice;

	@FXML
	private void initialize() {
		choice.setItems(availableChoices);
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
			translateToTeX();
		}
		if (selected.equals(itemArray[1])) {
			translateToMulti();
		}
		if (selected.equals(itemArray[2])) {
			translateToNum();
		}
	}

	//
	private void translateToTeX() {
		log.appendText("in Tex\n");
		// codeArea.setText(questionList.get(0));
	}

	private void translateToMulti() {
		log.appendText("in Multi\n");
		// <Q> </Q> で問題ごとに区切られていることを前提
		String doc = srcArea.getText();
		doc = doc.replaceAll("\n", "");
		doc = doc.replaceAll("\t", "");
		//codeArea.appendText(doc);
		List<String> quizList = new ArrayList<String>();
		String regex = "<Q>(.+?)</Q>";
		//regex = "(.+?)<s:.+?>";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(doc);
		while (m.find()) {
			String str = m.group();
			//System.out.println("<Q>置き換え前"+str);
			//codeArea.appendText("<Q>置き換え前"+str+"\n");
			str = str.replace("<Q>", "<p>");
			str = str.replace("</Q>", "</p>");
			//String str = m.group();
			//System.out.println("in find()"+str);
			quizList.add(str);
		}
		// <p></p>で囲まれた問題ごとに処理する。
		// 多肢選択問題の場合
		for (String target : quizList) {
			System.out.println("問題："+target);
			//codeArea.appendText("問題："+target+"\n");
			// <s:....>までの <q: ...>を処理したい
			regex = "(.+?)<s:.+?>";
			p = Pattern.compile(regex);
			m = p.matcher(target);
			String selectItems = null;
			while (m.find()) {
				String str = m.group();
				// str は<s:...>までの文字列
				System.out.println("最初のstr:"+str);
				String originalStr = new String(str);
				// いったん<s:....>を抜き出す。
				Pattern p_s = Pattern.compile("<s:.+?>");
				Matcher m_s = p_s.matcher(str);
				while (m_s.find()) {
					selectItems = m_s.group();
					//System.out.println("selectItems:"+selectItems);
					//codeArea.appendText("selectitems:"+selectItems+"\n");
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
					System.out.println("q_str:"+q_str);
					System.out.println("q置き換え前str:"+str);
					// 問題全体で<q:..> を置き換える
					str = str.replace(q_str, newStr);
				} // end of while(m2...
				System.out.println("消去前："+str);
				
				//codeArea.appendText("消去前："+str+"\n");
					// 小問題末尾の<s:...>を消す
				str = str.replace(selectItems, "");
				System.out.println("消去後str:"+str);
				target = target.replace(originalStr, str);
				System.out.println("置き換え後target:"+target);
				
				//codeArea.appendText(str+"\n");
				//System.out.println("----小問題終わり---");
				//codeArea.appendText("----小問題終わり---\n");
			} // end of while(m..小問題ごとの処理
			//System.out.println("多肢選択問題の固まり");
			//codeArea.appendText("多肢選択問題の固まり\n");
			//
			codeArea.appendText(target+"\n");
		}
	}

	private void translateToNum() {
		log.appendText("in Num\n");
	}
}
